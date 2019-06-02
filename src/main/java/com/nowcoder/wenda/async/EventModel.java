package com.nowcoder.wenda.async;

import java.util.HashMap;
import java.util.Map;

public class EventModel {
    private EventType type;   //事件发生的线程,比如点赞本身
    private int actorId;   //谁点赞,触发者
    private int entityType;  //给哪种类型点赞
    private int entityId;    //找到具体的点赞媒体
    private int entityOwnerId;   //与上面的entity相关联的人(可能是关注后给其发站内信的"其")

    //类似于vo,可以存其他变量,上面的使用的比较多才提取出来
    private Map<String, String> exts = new HashMap<String, String>();

    public EventModel() {  //默认的构造函数必须要有,因为下面写了新的构造函数,这边要加上

    }

    //构造方法,可以根据type直接拿到model
    public EventModel(EventType type) {
        this.type = type;
    }


    //返回值是eventmodel,return this是为了后面可以一直调用setXX.setXX.setXX这样链式
    //下面两个为了方便存储和设置一些字段
    public EventModel setExt(String key, String value) {
        exts.put(key, value);
        return this;
    }
    public String getExt(String key) {
        return exts.get(key);
    }



    //getter和setter
    public EventType getType() {
        return type;
    }

    public EventModel setType(EventType type) {
        this.type = type;
        return this;
    }

    public int getActorId() {
        return actorId;
    }

    public EventModel setActorId(int actorId) {
        this.actorId = actorId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public EventModel setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public EventModel setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityOwnerId() {
        return entityOwnerId;
    }

    public EventModel setEntityOwnerId(int entityOwnerId) {
        this.entityOwnerId = entityOwnerId;
        return this;
    }

    public Map<String, String> getExts() {
        return exts;
    }

    public EventModel setExts(Map<String, String> exts) {
        this.exts = exts;
        return this;
    }
}
