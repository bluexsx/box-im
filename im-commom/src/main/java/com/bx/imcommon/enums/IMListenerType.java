package com.bx.imcommon.enums;

public enum IMListenerType {

    PRIVATE_MESSAGE(0,"私聊消息"),
    GROUP_MESSAGE(1,"群聊消息");

    private Integer code;

    private String desc;

    IMListenerType(Integer index, String desc) {
        this.code =index;
        this.desc=desc;
    }

    public String getDesc() {
        return desc;
    }

    public Integer getCode(){
        return this.code;
    }
}
