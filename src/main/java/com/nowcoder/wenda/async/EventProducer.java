package com.nowcoder.wenda.async;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.wenda.util.JedisAdapter;
import com.nowcoder.wenda.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//异步就是进行业务解耦,方便扩展
//事件的入口,producer用redis实现插入,把事件推到队列里面去,consumer是取出来
@Service
public class EventProducer {
    @Autowired
    JedisAdapter jedisAdapter;   //看成是一个队列在这里

    public boolean fireEvent(EventModel eventModel) {
        try {
            String json = JSONObject.toJSONString(eventModel);   //把model中信息存到json串中,作为值
            String key = RedisKeyUtil.getEventQueueKey();
            jedisAdapter.lpush(key, json);   //将key-value放到队列中,用redis的方式,是两个字符串,第一个是key的字符串,然后是model的json串
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
