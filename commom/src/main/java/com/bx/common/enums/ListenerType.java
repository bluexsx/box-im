package com.bx.common.enums;

public enum ListenerType {

    PRIVATE_MESSAGE(0,"私聊消息"),
    GROUP_MESSAGE(1,"群聊消息");

    private Integer code;

    private String desc;

    ListenerType(Integer index, String desc) {
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
