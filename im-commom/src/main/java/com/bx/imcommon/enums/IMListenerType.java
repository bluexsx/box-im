package com.bx.imcommon.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum IMListenerType{
    ALL(0,"全部消息"),
    PRIVATE_MESSAGE(1,"私聊消息"),
    GROUP_MESSAGE(2,"群聊消息");

    private Integer code;

    private String desc;



    public Integer code(){
        return this.code;
    }

}
