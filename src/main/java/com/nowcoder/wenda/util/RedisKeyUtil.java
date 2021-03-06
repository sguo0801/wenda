package com.nowcoder.wenda.util;

//针对不同业务生成不同的key,避免命令混淆弄出错,这里是针对不同业务加不同前缀
public class RedisKeyUtil {
    private static String SPLIT = ":";
    private static String BIZ_LIKE = "LIKE";
    private static String BIZ_DISLIKE = "DISLIKE";
    private static String BIZ_EVENTQUEUE = "EVENT_QUEUE";
    // 获取粉丝
    private static String BIZ_FOLLOWER = "FOLLOWER";
    // 关注对象
    private static String BIZ_FOLLOWEE = "FOLLOWEE";
    private static String BIZ_TIMELINE = "TIMELINE";

    public static String getLikeKey(int entityType, int entityId) {
        return BIZ_LIKE + SPLIT + entityType + SPLIT + entityId;    //根据类型和id生成喜欢的类型key
    }

    public static String getDisLikeKey(int entityType, int entityId) {
        return BIZ_DISLIKE + SPLIT + String.valueOf(entityType) + SPLIT + String.valueOf(entityId);
    }

    public static String getEventQueueKey() {
        return BIZ_EVENTQUEUE;
    }

    // 某个实体的粉丝key,每种类型都有粉丝
    //比如我关注了一个问题,我就是这个问题的粉丝.我的关注列表有这个问题.
    public static String getFollowerKey(int entityType, int entityId) {    //这个问题实体(用entity指定这个问题)  粉丝的key
        return BIZ_FOLLOWER + SPLIT + String.valueOf(entityType) + SPLIT + String.valueOf(entityId);
    }

    // 每个用户对某类实体的关注key(关注了谁),比如可以是该用户关注的所有问题,不需要entityid
    public static String getFolloweeKey(int userId, int entityType) {   //某一个用户userid关注某一类实体entitytype 的key(关注对象)
        return BIZ_FOLLOWEE + SPLIT + String.valueOf(userId) + SPLIT + String.valueOf(entityType);
    }

    public static String getTimelineKey(int userId) {
        return BIZ_TIMELINE + SPLIT + String.valueOf(userId);
    }
}
