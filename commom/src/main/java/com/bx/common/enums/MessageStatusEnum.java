package com.bx.common.enums;


public enum MessageStatusEnum {

    UNREAD(0,"未读"),
    ALREADY_READ(1,"已读"),
    RECALL(2,"已撤回");

    private Integer code;

    private String desc;

    MessageStatusEnum(Integer index, String desc) {
        this.code =index;
        this.desc=desc;
    }

    public static MessageStatusEnum fromCode(Integer code){
        for (MessageStatusEnum typeEnum:values()) {
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
