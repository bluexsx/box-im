package com.bx.implatform.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MessageType {

    /**
     * 文字
     */
    TEXT(0, "文字"),
    /**
     * 图片
     */
    IMAGE(1, "图片"),
    /**
     * 文件
     */
    FILE(2, "文件"),
    /**
     * 音频
     */
    AUDIO(3, "音频"),
    /**
     * 视频
     */
    VIDEO(4, "视频"),
    /**
     * 撤回
     */
    RECALL(10, "撤回"),
    /**
     * 已读
     */
    READED(11, "已读"),

    /**
     * 消息已读回执
     */
    RECEIPT(12, "消息已读回执"),
    /**
     * 时间提示
     */
    TIP_TIME(20,"时间提示"),
    /**
     * 文字提示
     */
    TIP_TEXT(21,"文字提示"),

    /**
     * 消息加载标记
     */
    LOADDING(30,"加载中"),

    RTC_CALL_VOICE(100, "语音呼叫"),
    RTC_CALL_VIDEO(101, "视频呼叫"),
    RTC_ACCEPT(102, "接受"),
    RTC_REJECT(103, "拒绝"),
    RTC_CANCEL(104, "取消呼叫"),
    RTC_FAILED(105, "呼叫失败"),
    RTC_HANDUP(106, "挂断"),
    RTC_CANDIDATE(107, "同步candidate"),
    RTC_GROUP_SETUP(200,"发起群视频通话"),
    RTC_GROUP_ACCEPT(201,"接受通话呼叫"),
    RTC_GROUP_REJECT(202,"拒绝通话呼叫"),
    RTC_GROUP_FAILED(203,"拒绝通话呼叫"),
    RTC_GROUP_CANCEL(204,"取消通话呼叫"),
    RTC_GROUP_QUIT(205,"退出通话"),
    RTC_GROUP_INVITE(206,"邀请进入通话"),
    RTC_GROUP_JOIN(207,"主动进入通话"),
    RTC_GROUP_OFFER(208,"推送offer信息"),
    RTC_GROUP_ANSWER(209,"推送answer信息"),
    RTC_GROUP_CANDIDATE(210,"同步candidate"),
    RTC_GROUP_DEVICE(211,"设备操作"),
    ;

    private final Integer code;

    private final String desc;


    public Integer code() {
        return this.code;
    }
}
