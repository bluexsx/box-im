package com.bx.imcommon.model;

import com.bx.imcommon.enums.IMSendStatus;
import lombok.Data;

@Data
public class SendResult<T> {

    /*
     * 接收者id
     */
    private Long recvId;

    /*
     * 发送状态
     */
    private IMSendStatus status;

    /*
     * 失败原因
     */
    private String failReason="";

    /*
     * 消息体(透传)
     */
    private T messageInfo;

}
