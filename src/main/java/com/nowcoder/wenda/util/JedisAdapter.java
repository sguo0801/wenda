package com.nowcoder.wenda.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nowcoder.wenda.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.Tuple;

import java.io.IOException;
import java.util.List;
import java.util.Set;


@Service
public class JedisAdapter implements InitializingBean {  //实现初始化变量
    private static final Logger logger = LoggerFactory.getLogger(JedisAdapter.class);
    private JedisPool pool;

    public static void print(int index, Object obj) {
        System.out.println(String.format("%d, %s", index, obj.toString()));
    }

    public static void main(String[] argv) {
        Jedis jedis = new Jedis("redis://localhost:6379/9");   //一共16个数据库,选择第九个
        jedis.flushDB();  //把9库清空,fiushAll是把所有数据库清空

        jedis.set("hello", "world");
        print(1, jedis.get("hello"));
        jedis.rename("hello", "newhello");
        jedis.setex("hello2", 15, "world");  //过15秒删除,验证码

        jedis.set("pv", "100");
        jedis.incr("pv");
        jedis.incrBy("pv", 5);
        print(2, jedis.get("pv"));
        jedis.decr("pv");
        jedis.decrBy("pv", 2);
        print(3, jedis.get("pv"));

        print(4, jedis.keys("*"));

        String listName = "list";
        jedis.del(listName);
        for (int i = 0; i < 10; ++i) {
            jedis.lpush(listName, "a" + String.valueOf(i));  //list的插入
        }

        print(5, jedis.lrange(listName, 0, 10));   //取值范围从前,索引从到10.
        print(6, jedis.lrange(listName, 0, 3));
        print(7, jedis.llen(listName));
        print(8, jedis.lpop(listName));
        print(9, jedis.llen(listName));


        //Hash
        String userKey = "userxx";
        jedis.hset(userKey, "name", "Jim");
        jedis.hset(userKey, "age", "12");
        jedis.hset(userKey, "phone", "18181818818");
        print(12, jedis.hget(userKey, "name"));
        print(13, jedis.hgetAll(userKey));

        jedis.hdel(userKey, "phone");
        print(14, jedis.hgetAll(userKey));

        print(15, jedis.hvals(userKey));
        print(15, jedis.hkeys(userKey));


        //set   点赞点踩进行去重,###可以按数值进行排序###ologn搜索
        String likeKey1 = "commentLike1";
        String likeKey2 = "commentLike2";
        for (int i = 0; i < 10; ++i) {
            jedis.sadd(likeKey1, String.valueOf(i));
            jedis.sadd(likeKey2, String.valueOf(i * i));
        }
        print(16, jedis.smembers(likeKey1));
        print(17, jedis.smembers(likeKey2));

        print(18, jedis.sunion(likeKey1, likeKey2));
        print(18, jedis.sdiff(likeKey1, likeKey2));
        print(18, jedis.sinter(likeKey1, likeKey2));

        print(19, jedis.sismember(likeKey1, "12"));
        print(19, jedis.sismember(likeKey2, "16"));
        jedis.srem(likeKey1, "5");
        print(20, jedis.smembers(likeKey1));

        jedis.smove(likeKey2, likeKey1, "25");
        print(21, jedis.smembers(likeKey1));
        print(21, jedis.smembers(likeKey2));

        print(22, jedis.scard(likeKey1));  //人数,可以看关注的人数

        //这里是按分值score排序,是有序集合(类似优先队列)
        String rankKey = "rankKey";
        jedis.zadd(rankKey, 15, "jim");
        jedis.zadd(rankKey, 60, "Ben");
        jedis.zadd(rankKey, 90, "Lee");
        jedis.zadd(rankKey, 75, "Lucy");
        jedis.zadd(rankKey, 80, "Mei");
        print(30, jedis.zcard(rankKey));
        print(31, jedis.zcount(rankKey, 61, 100));
        print(32, jedis.zscore(rankKey, "Lucy"));
        jedis.zincrby(rankKey, 2, "Luc");
        print(33, jedis.zscore(rankKey, "Luc"));
        print(34, jedis.zrange(rankKey, 0, 100));   //进行排名,索引为名次(0为最开始)
        print(34, jedis.zrange(rankKey, 1, 3));
        print(34, jedis.zrevrange(rankKey, 1, 3));   //从高分到低分排序

        //遍历该集合60~100分的
        for (Tuple tuple : jedis.zrangeByScoreWithScores(rankKey, "60", "100")) {
            print(37, tuple.getElement() + " : " + String.valueOf(tuple.getScore()));
        }


        print(38, jedis.zrank(rankKey, "Ben"));
        print(38, jedis.zrevrank(rankKey, "Ben"));
        print(38, jedis.zrevrank(rankKey, "Ben"));

        String setKey = "zset";
        jedis.zadd(setKey, 1, "a");
        jedis.zadd(setKey, 1, "b");
        jedis.zadd(setKey, 1, "c");
        jedis.zadd(setKey, 1, "d");
        jedis.zadd(setKey, 1, "e");

        print(40, jedis.zlexcount(setKey, "-", "+"));   //边界区间
        print(40, jedis.zlexcount(setKey, "(b", "(d"));
        print(40, jedis.zlexcount(setKey, "(b", "[d"));
        jedis.zrem(setKey, "b");
        print(41, jedis.zrange(setKey, 0, 10));
        jedis.zremrangeByLex(setKey, "(c", "+");
        print(42, jedis.zrange(setKey, 0, 2));

        //连接池不可用
//        JedisPool pool = new JedisPool();  //默认8个,不放开的话
//        for (int i = 0; i < 100; ++i){
//            Jedis j = pool.getResource();
//            print(45, j.get("pv"));
//            j.close();
//        }

        //####这边就是异步的主要数据存储过程,序列化和反序列化在缓存中存取数据
        User user = new User();
        user.setHeadUrl("a.png");
        user.setName("xx");
        user.setPassword("ppp");
        user.setSalt("salt");
        user.setId(1);
        print(46, JSONObject.toJSONString(user));
        jedis.set("user1", JSONObject.toJSONString(user));  //把user的数据通过json串存在redis中

        String value = jedis.get("user1");
        User user2 = JSON.parseObject(value, User.class);   //反序列化把存在redis的字符串取出来,并且转化成类
        print(47, user2);    //这时候打印的就是user这个类的地址
        int k = 2;


    }

    //进行初始化redis连接池,都是直接调用redis方法,之前连接到连接池
    @Override
    public void afterPropertiesSet() throws Exception {
        pool = new JedisPool("redis://localhost:6379/10");

    }

    //进行向redis中添加数据,一个key-value参数,sadd方法默认返回值为long
    public long sadd(String key, String value){
        Jedis jedis = null;  //初始为null
        try{
            jedis = pool.getResource();   //先连接到连接池
            return jedis.sadd(key, value);  //进行添加数据到该redis库.
        }catch (Exception e){
            logger.info("发生异常" + e.getMessage());
        }finally {
            if (jedis != null){
                jedis.close();   //不关闭默认只有8条.
            }
        }
        return 0;
    }
//    public long sadd(String key, String value) {
//        Jedis jedis = null;
//        try {
//            jedis = pool.getResource();
//            return jedis.sadd(key, value);
//        } catch (Exception e) {
//            logger.error("发生异常" + e.getMessage());
//        } finally {
//            if (jedis != null) {
//                jedis.close();
//            }
//        }
//        return 0;
//    }

    //删除key-value元素
    public long srem(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.srem(key, value);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0;
    }

    //求集合中的数量
    public long scard(String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.scard(key);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0;
    }

    //判断是否存在该元素
    public boolean sismember(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.sismember(key, value);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return false;
    }

    public List<String> brpop(int timeout, String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.brpop(timeout, key);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    public long lpush(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.lpush(key, value);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0;
    }

    public List<String> lrange(String key, int start, int end) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.lrange(key, start, end);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }


    //关注列表
    public Jedis getJedis() {
        return pool.getResource();
    }

    //开启
    public Transaction multi(Jedis jedis) {
        try {
            return jedis.multi();      //开启事务,要执行多个命令(原子性)
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
        }
        return null;
    }

    //执行
    public List<Object> exec(Transaction tx, Jedis jedis) {
        try {
            return tx.exec();  //在一个事务中排队执行命令
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
            tx.discard();   //回滚
        } finally {
            if (tx != null) {
                try {
                    tx.close();
                } catch (IOException ioe) {
                    logger.error("发生异常" + ioe.getMessage());
                }
            }

            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    //添加到关注列表,score为时间(方便排序新旧),返回的long可以看是否>0判断添加成功
    public long zadd(String key, double score, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.zadd(key, score, value);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0;
    }

    public long zrem(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.zrem(key, value);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0;
    }

    //排序返回的是String集合,后面要转成Integer的ID列表.这里是要找到关注的所有粉丝
    public Set<String> zrange(String key, int start, int end) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.zrange(key, start, end);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    public Set<String> zrevrange(String key, int start, int end) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.zrevrange(key, start, end);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    //看队列里有多少数字
    public long zcard(String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.zcard(key);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0;
    }

    public Double zscore(String key, String member) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.zscore(key, member);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

}
