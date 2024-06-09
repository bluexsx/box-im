package com.bx.implatform.contant;

public final class RedisKey {

    private RedisKey() {
    }

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
     * 缓存前缀
     */
    public static final String IM_CACHE = "im:cache:";
    /**
     * 缓存是否好友：bool
     */
    public static final String IM_CACHE_FRIEND = IM_CACHE + "friend";
    /**
     * 缓存群聊信息
     */
    public static final String IM_CACHE_GROUP = IM_CACHE + "group";
    /**
     * 缓存群聊成员id
     */
    public static final String IM_CACHE_GROUP_MEMBER_ID = IM_CACHE + "group_member_ids";

    /**
     * 分布式锁前缀
     */
    public static final String IM_LOCK = "im:lock:";

    /**
     * 分布式锁前缀
     */
    public static final String IM_LOCK_RTC_GROUP = IM_LOCK + "rtc:group";

}
