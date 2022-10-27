package com.lx.common.enums;

public enum WSCmdEnum {

    HEARTBEAT(0,"心跳"),
    SINGLE_MESSAGE(1,"单发消息"),
    GROUP_MESSAGE(2,"群发消息");


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

