package com.lx.common.enums;

public enum WSCmdEnum {

    LOGIN(0,"登陆"),
    HEART_BEAT(1,"心跳"),
    FORCE_LOGUT(2,"强制下线"),
    PRIVATE_MESSAGE(3,"私聊消息"),
    GROUP_MESSAGE(4,"群发消息");


    private Integer code;

    private String desc;

    WSCmdEnum(Integer index, String desc) {
        this.code =index;
        this.desc=desc;
    }

    public static WSCmdEnum fromCode(Integer code){
        for (WSCmdEnum typeEnum:values()) {
            if (typeEnum.code.equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }


    public String getDesc() {
        return desc;
    }

    public Integer getCode(){
        return this.code;
    }


}

