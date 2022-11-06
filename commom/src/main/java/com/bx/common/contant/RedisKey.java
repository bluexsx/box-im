package com.bx.common.contant;

public class RedisKey {

    // im-server最大id,从0开始递增
    public final static String  IM_MAX_SERVER_ID = "im:max_server_id";
    // 用户ID所连接的IM-server的ID
    public final static String  IM_USER_SERVER_ID = "im:user:server_id:";
    // 未读私聊消息队列
    public final static String IM_UNREAD_PRIVATE_MESSAGE = "im:unread:private:";
    // 未读群聊消息队列
    public final static String IM_UNREAD_GROUP_MESSAGE = "im:unread:group:";
    // 已读私聊消息id队列
    public final static String IM_READED_PRIVATE_MESSAGE_ID = "im:readed:private:id";
    // 已读群聊消息位置(已读最大id)
    public final static String IM_GROUP_READED_POSITION = "im:readed:group:position:";
    // 缓存前缀
    public final static String  IM_CACHE = "im:cache:";
    // 缓存是否好友：bool
    public final static String  IM_CACHE_FRIEND = IM_CACHE+"friend";
    // 缓存群聊信息
    public final static String  IM_CACHE_GROUP = IM_CACHE+"group";
    // 缓存群聊成员id
    public final static String IM_CACHE_GROUP_MEMBER_ID = IM_CACHE+"group_member_ids";

}
