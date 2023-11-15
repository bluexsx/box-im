package com.bx.imcommon.model;

import lombok.Data;

@Data
public class IMSendResult<T> {

    /**
     * 发送方
     */
    private IMUserInfo sender;

    /**
     * 接收方
     */
    private IMUserInfo receiver;

    /**
     * 发送状态 IMCmdType
     */
    private Integer code;

    /**
     *  消息内容
     */
    private T data;

}
