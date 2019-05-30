package com.nowcoder.wenda.model;

//对应的是comment中的评论位置类型,因为entityType为int类型,这里对应不同的位置所对应的int类型.
public class EntityType {
    public static int ENTITY_QUESTION = 1;  //在问题下面评论就是用这个静态常量,可以看commentcontroller
    public static int ENTITY_COMMENT = 2;
    public static int ENTITY_USER = 3;

}
