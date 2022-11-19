package com.bx.common.enums;


public enum MessageStatus {

    UNREAD(0,"未读"),
    ALREADY_READ(1,"已读"),
    RECALL(2,"已撤回");

    private Integer code;

    private String desc;

    MessageStatus(Integer index, String desc) {
        this.code =index;
        this.desc=desc;
    }

    public static MessageStatus fromCode(Integer code){
        for (MessageStatus typeEnum:values()) {
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
