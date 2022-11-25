package com.bx.implatform.enums;


public enum MessageType {

    TEXT(0,"文字"),
    FILE(1,"文件"),
    IMAGE(2,"图片"),
    VIDEO(3,"视频"),
    TIP(10,"系统提示"),

    RTC_CALL(101,"呼叫"),
    RTC_ACCEPT(102,"接受"),
    RTC_REJECT(103, "拒绝"),
    RTC_CANCEL(104,"取消呼叫"),
    RTC_FAILED(105,"呼叫失败"),
    RTC_HANDUP(106,"挂断"),
    RTC_CANDIDATE(107,"同步candidate");

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
