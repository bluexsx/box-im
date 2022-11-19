package com.bx.implatform.enums;


public enum MessageType {

    TEXT(0,"文字"),
    FILE(1,"文件"),
    IMAGE(2,"图片"),
    VIDEO(3,"视频"),
    TIP(10,"系统提示");

    private Integer code;

    private String desc;

    MessageType(Integer index, String desc) {
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
