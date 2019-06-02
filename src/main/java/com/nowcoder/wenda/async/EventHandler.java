package com.nowcoder.wenda.async;

import java.util.List;

//专门处理event,设计模式策略模式
public interface EventHandler {
    void doHandle(EventModel model);  //处理

    List<EventType> getSupportEventTypes();  //关注的所有type,只要是这些类型都用我这个接口的doHandle来处理
}
