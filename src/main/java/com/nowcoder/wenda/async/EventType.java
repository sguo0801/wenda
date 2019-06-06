package com.nowcoder.wenda.async;

//表示事件类型(枚举型Enum)
public enum EventType {
    LIKE(0),
    COMMENT(1),
    LOGIN(2),
    MAIL(3),
    FOLLOW(4),
    UNFOLLOW(5),
    ADD_QUESTION(6);   //注意逗号,只有一个分号


    private int value;
    EventType(int value) { this.value = value; }
    public int getValue() { return value; }
}
