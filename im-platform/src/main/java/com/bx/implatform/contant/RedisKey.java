package com.bx.implatform.contant;

public final class RedisKey {

    /**
     *  用户状态 无值:空闲  1:正在忙
     */
    public static final String IM_USER_STATE = "im:user:state";

    /**
     * 私聊会话已读位置(已读最大id)
     */
    public static final String IM_PRIVATE_READED_POSITION = "im:readed:private:position";

    /**
     * 已读群聊消息位置(已读最大id)
     */
    public static final String IM_GROUP_READED_POSITION = "im:readed:group:position";

    /**
     * 私聊会话消息最大id
     */
    public static final String IM_PRIVATE_MESSAGE_MAX_ID = "im:message:private:max_id";

    /**
     * 群聊会话消息最大id
     */
    public static final String IM_GROUP_MESSAGE_MAX_ID = "im:message:group:max_id";

    /**
     * 私聊会话消息最大序列号
     */
    public static final String IM_PRIVATE_MESSAGE_MAX_SEQ = "im:message:private:max_seq";

    /**
     * 群聊离线消息时间窗口对应的 id 下界（窗口外最后一条消息 id）
     */
    public static final String IM_GROUP_OFFLINE_TIME_MIN_ID = "im:message:group:offline:time_bound_id";

    /**
     * 群聊会话消息最大id
     */
    public static final String IM_GROUP_MESSAGE_MAX_SEQ = "im:message:group:max_seq";

    /**
     * 分布式锁-保存私聊会话消息
     */
    public static final String IM_LOCK_PRIVATE_MESSAGE_SAVE = "im:lock:message:private:save";


    /**
     * 分布式锁-私聊会话消息最大序列号
     */
    public static final String IM_LOCK_PRIVATE_MESSAGE_MAX_SEQ = "im:lock:message:private:max_seq";

    /**
     * 分布式锁-群聊会话消息最大序列号
     */
    public static final String IM_LOCK_GROUP_MESSAGE_MAX_SEQ = "im:lock:message:group:max_seq";


    /**
     * 分布式锁-保存群聊会话消息
     */
    public static final String IM_LOCK_GROUP_MESSAGE_SAVE = "im:lock:message:group:save";

    /**
     * webrtc 单人通话
     */
    public static final String IM_WEBRTC_PRIVATE_SESSION = "im:webrtc:private:session";

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
    public static final String IM_QUEUE_GROUP_UNBAN = "im:queue:group:unban";


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
     * 缓存消息删除记录
     */
    public static final String IM_CACHE_MESSAGE_DELETION = "im:cache:message:deletion";
    /**
     * 重复提交
     */
    public static final String IM_REPEAT_SUBMIT = "im:repeat:submit";

    /**
     * 分布式锁-添加好友
     */
    public static final String IM_LOCK_FRIEND_ADD =  "im:lock:friend:add";

    /**
     * 分布式锁-进入群聊
     */
    public static final String IM_LOCK_GROUP_ENTER =  "im:lock:group:enter";


    /**
     * 分布式锁-清理过期文件
     */
    public static final String IM_LOCK_FILE_TASK =  "im:lock:task:file";

    /**
     * 群成员最大版本号
     */
    public static final String IM_GROUP_MEMBER_MAX_VERSION = "im:group:member:max_version";


    /**
     * 好友信息最大版本号
     */
    public static final String IM_FRIEND_MAX_VERSION = "im:friend:max_version";

}
