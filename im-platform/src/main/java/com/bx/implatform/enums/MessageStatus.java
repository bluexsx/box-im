package com.bx.implatform.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MessageStatus {

    /**
     * 文件
     */
    UNSEND(0, "未送达"),
    /**
     * 文件
     */
    SENDED(1, "送达"),
    /**
     * 撤回
     */
    RECALL(2, "撤回"),
    /**
     * 已读
     */
    READED(3, "已读");

    private final Integer code;

    private final String desc;


    public Integer code() {
        return this.code;
    }
}
