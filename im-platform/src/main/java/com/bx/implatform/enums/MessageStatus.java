package com.bx.implatform.enums;


public enum MessageStatus {

    UNREAD(0,"未读"),
    ALREADY_READ(1,"已读"),
    RECALL(2,"已撤回");

    private final Integer code;

    private final String desc;

    MessageStatus(Integer index, String desc) {
        this.code =index;
        this.desc=desc;
    }

    public String description() {
        return desc;
    }

    public Integer code(){
        return this.code;
    }
}
