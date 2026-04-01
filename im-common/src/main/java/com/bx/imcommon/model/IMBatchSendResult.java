package com.bx.imcommon.model;

import lombok.Data;

import java.util.List;

@Data
public class IMBatchSendResult<T> {

    /**
     * 发送方
     */
    private IMUserInfo sender;

    /**
     * 接收方
     */
    private List<IMUserInfo> receivers;

    /**
     * 发送状态编码 IMSendCode
     */
    private Integer code;

    /**
     * 消息内容
     */
    private T data;

}
