package com.bx.imcommon.contant;

public final class IMRedisKey {


    /**
     * im-server最大id,从0开始递增
     */
    public static final String  IM_MAX_SERVER_ID = "im:max_server_id";
    /**
     * 用户ID所连接的IM-server的ID
     */
    public static final String  IM_USER_SERVER_ID = "im:user:server_id";

    /**
     * 同用户多终端共 slot，保证单用户查询在线不会分发到多个节点，提升性能
     */
    public static String userServerIdKey(Long userId, Integer terminal) {
        return String.join(":", IM_USER_SERVER_ID, "{" + userId + "}", terminal.toString());
    }

    /**
     * 系统消息队列
     */
    public static final String IM_MESSAGE_SYSTEM_QUEUE = "im:message:system";
    /**
     * 私聊消息队列
     */
    public static final String IM_MESSAGE_PRIVATE_QUEUE = "im:message:private";
    /**
     * 群聊消息队列
     */
    public static final String IM_MESSAGE_GROUP_QUEUE = "im:message:group";

    /**
     * 系统消息发送结果队列
     */
    public static final String IM_RESULT_SYSTEM_QUEUE = "im:result:system";
    /**
     * 私聊消息发送结果队列
     */
    public static final String IM_RESULT_PRIVATE_QUEUE = "im:result:private";
    /**
     * 群聊消息发送结果队列
     */
    public static final String IM_RESULT_GROUP_QUEUE = "im:result:group";

    /**
     * 用户事件队列
     */
    public static final String IM_USER_EVENT_QUEUE = "im:user:event";

    /**
     * 强制用户退出队列
     */
    public static final String IM_USER_FORCE_LOGOUT_QUEUE = "im:user:force_logout";


}
