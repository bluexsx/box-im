package com.bx.imcommon.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum IMCmdType {

    /**
     * 登录
     */
    LOGIN(0, "登录"),
    /**
     * 心跳
     */
    HEART_BEAT(1, "心跳"),
    /**
     * 强制下线
     */
    FORCE_LOGUT(2, "强制下线"),
    /**
     * 私聊消息
     */
    PRIVATE_MESSAGE(3, "私聊消息"),
    /**
     * 群发消息
     */
    GROUP_MESSAGE(4, "群发消息"),
    /**
     * 系统消息
     */
    SYSTEM_MESSAGE(5,"系统消息");


    private final Integer code;

    private final String desc;


    public static IMCmdType fromCode(Integer code) {
        for (IMCmdType typeEnum : values()) {
            if (typeEnum.code.equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }


    public Integer code() {
        return this.code;
    }


}

