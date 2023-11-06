package com.bx.implatform.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MessageType {

    TEXT(0,"文字"),
    IMAGE(1,"图片"),
    FILE(2,"文件"),
    AUDIO(3,"音频"),
    VIDEO(4,"视频"),
    RECALL(10,"撤回"),
    READED(11, "已读"),

    RTC_CALL(101,"呼叫"),
    RTC_ACCEPT(102,"接受"),
    RTC_REJECT(103, "拒绝"),
    RTC_CANCEL(104,"取消呼叫"),
    RTC_FAILED(105,"呼叫失败"),
    RTC_HANDUP(106,"挂断"),
    RTC_CANDIDATE(107,"同步candidate");

    private final Integer code;

    private final String desc;


    public Integer code(){
        return this.code;
    }
}
