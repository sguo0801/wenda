package com.nowcoder.wenda.service;

import com.nowcoder.wenda.util.JedisAdapter;
import com.nowcoder.wenda.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
//redis业务,没有model.这里直接设置key-value

//赞踩的业务,这里不针对评论这一种,什么样的业务用什么样的key
//这里用的都是集合方法,sadd,scard,srem,等所以每次String key都是找到相同的key
@Service
public class LikeService {
    @Autowired
    JedisAdapter jedisAdapter;


    public long getLikeCount(int entityType, int entityId) {   //当前喜欢的人数
        String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);   //先 生成业务对应的key
        return jedisAdapter.scard(likeKey);
    }

    public int getLikeStatus(int userId, int entityType, int entityId) {  //当前用户对点赞还是点踩的台服
        String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
        if (jedisAdapter.sismember(likeKey, String.valueOf(userId))) {   //喜欢的key中有这个user则返回1代表点赞状态
            return 1;
        }
        String disLikeKey = RedisKeyUtil.getDisLikeKey(entityType, entityId);
        return jedisAdapter.sismember(disLikeKey, String.valueOf(userId)) ? -1 : 0;   //不喜欢是-1,没有点就是0
    }

    public long like(int userId, int entityType, int entityId) {
        String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
        jedisAdapter.sadd(likeKey, String.valueOf(userId));   //代表当前这个key存的是点赞的人的id的String类型(因为接口参数都是String类型)

        String disLikeKey = RedisKeyUtil.getDisLikeKey(entityType, entityId);
        jedisAdapter.srem(disLikeKey, String.valueOf(userId));   //如果点赞啦,就不能点踩,这里把该userid从点踩的key中删除.

        return jedisAdapter.scard(likeKey);   //返回喜欢的人数,这个方法只是对赞踩的key对应的值进行变化 后的数量
    }

    public long disLike(int userId, int entityType, int entityId) {
        String disLikeKey = RedisKeyUtil.getDisLikeKey(entityType, entityId);
        jedisAdapter.sadd(disLikeKey, String.valueOf(userId));

        String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
        jedisAdapter.srem(likeKey, String.valueOf(userId));

        return jedisAdapter.scard(likeKey);   //返回的依然是喜欢的人数
    }
}
