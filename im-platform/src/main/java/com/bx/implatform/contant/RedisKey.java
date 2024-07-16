package com.bx.implatform.contant;

public final class RedisKey {

    /**
     *  用户状态 无值:空闲  1:正在忙
     */
    public static final String IM_USER_STATE = "im:user:state";
    /**
     * 已读群聊消息位置(已读最大id)
     */
    public static final String IM_GROUP_READED_POSITION = "im:readed:group:position";
    /**
     * webrtc 单人通话
     */
    public static final String IM_WEBRTC_PRIVATE_SESSION = "im:webrtc:private:session";
    /**
     * webrtc 群通话
     */
    public static final String IM_WEBRTC_GROUP_SESSION = "im:webrtc:group:session";

    /**
     * 用户被封禁消息队列
     */
    public static final String IM_QUEUE_USER_BANNED = "im:queue:user:banned";

    /**
     * 群聊被封禁消息队列
     */
    public static final String IM_QUEUE_GROUP_BANNED = "im:queue:group:banned";

    /**
     * 群聊解封消息队列
     */
    public static final String IM_QUEUE_GROUP_UNBAN = "im:queue:user:unban";


    /**
     * 缓存是否好友：bool
     */
    public static final String IM_CACHE_FRIEND = "im:cache:friend";
    /**
     * 缓存群聊信息
     */
    public static final String IM_CACHE_GROUP =  "im:cache:group";
    /**
     * 缓存群聊成员id
     */
    public static final String IM_CACHE_GROUP_MEMBER_ID = "im:cache:group_member_ids";
    /**
     * 分布式锁前缀
     */
    public static final String IM_LOCK_RTC_GROUP =  "im:lock:rtc:group";

}
