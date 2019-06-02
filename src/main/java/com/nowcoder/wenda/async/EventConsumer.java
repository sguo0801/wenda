package com.nowcoder.wenda.async;

import com.alibaba.fastjson.JSON;
import com.nowcoder.wenda.util.JedisAdapter;
import com.nowcoder.wenda.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//处理队列里面的所有event,最先知道有哪些event,把handle和event的关系建立起来
@Service
public class EventConsumer implements InitializingBean, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    //config里面是所有能处理的事件类型和对应的处理的几个实现类(实现类是一个个执行实现异步)
    private Map<EventType, List<EventHandler>> config = new HashMap<EventType, List<EventHandler>>();  //从event中取出event的类型,找到要处理的一批event然后一个一个处理
    private ApplicationContext applicationContext;

    @Autowired
    JedisAdapter jedisAdapter;

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, EventHandler> beans = applicationContext.getBeansOfType(EventHandler.class);  //不知道有多少eventHandler接口的实现类(handler中所有)
        // 1.现在是从上下文找出所有实现类,知道我关心什么type的event,每个handler实现类就会看自己能处理的event类型,然后最后好进行dohandler
        if (beans != null) {   //遍历所有的handler,该实现类肯定有handler接口中的getSupportEventTypes方法,里面是能处理的eventtype类型,放在List中
            for (Map.Entry<String, EventHandler> entry : beans.entrySet()) {  //EventHandler就是实现类
                List<EventType> eventTypes = entry.getValue().getSupportEventTypes();   //每个实现类关心的类型

                //2.找到所有能处理的的event,并跟实现类关联起来(一起放在config中,key是类型,值就是实现类)
                for (EventType type : eventTypes) {
                    if (!config.containsKey(type)) {
                        config.put(type, new ArrayList<EventHandler>());
                    }
                    config.get(type).add(entry.getValue());  //这样在有event进来的时候,可以在对应的list中找到handler一个一个进行处理
                }
            }
        }

        //找队列里的event进行处理,这里也可以起一个线程池,多条同时执行
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {  //一直在轮询,看队列中是否有事件(就是key是否存在).
                    String key = RedisKeyUtil.getEventQueueKey();
                    List<String> events = jedisAdapter.brpop(0, key);  //队列是用redis实现的,现在开始取,从右侧(先进先出),超时时间设置为0,如果队列中没有event就可以一直卡着

                    //这时找到了event,但是第一个值是key-value的key,所以要去掉.(从producer里看到,key后面才是model,model里面才有类型.
                    for (String message : events) {
                        if (message.equals(key)) {
                            continue;
                        }

                        //反序列化解析eventModel,然后找handler 开始处理
                        EventModel eventModel = JSON.parseObject(message, EventModel.class);
                        if (!config.containsKey(eventModel.getType())) {
                            logger.error("不能识别的事件");  //不能识别,就看队列中的下一个事件
                            continue;
                        }

                        //现在是可以识别的事件类型,开始找对应的所有处理实现类handler
                        for (EventHandler handler : config.get(eventModel.getType())) {   //每个事件对应很多实现类,一个一个来实现(dohandler)
                            handler.doHandle(eventModel);
                        }
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
