package com.bx.imcommon.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum IMListenerType {
    /**
     * 全部消息
     */
    ALL(0, "全部消息"),
    /**
     * 私聊消息
     */
    PRIVATE_MESSAGE(1, "私聊消息"),
    /**
     * 群聊消息
     */
    GROUP_MESSAGE(2, "群聊消息"),
    /**
     * 系统消息
     */
    SYSTEM_MESSAGE(3, "群聊消息");



    private final Integer code;

    private final String desc;

    public Integer code() {
        return this.code;
    }

}
