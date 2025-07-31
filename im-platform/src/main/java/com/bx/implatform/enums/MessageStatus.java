package com.bx.implatform.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MessageStatus {

    /**
     * 等待推送(未送达)
     */
    PENDING(0, "等待推送"),
    /**
     * 已送达(未读)
     */
    DELIVERED(1, "已送达"),
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
