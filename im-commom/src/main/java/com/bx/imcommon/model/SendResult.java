package com.bx.imcommon.model;

import com.bx.imcommon.enums.IMSendCode;
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
    private IMSendCode code;

    /*
     * 消息体(透传)
     */
    private T messageInfo;

}
