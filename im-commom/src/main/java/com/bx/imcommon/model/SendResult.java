package com.bx.imcommon.model;

import com.bx.imcommon.enums.IMSendStatus;
import lombok.Data;

@Data
public class SendResult<T> {

    private Long recvId;

    private IMSendStatus status;

    private String failReason="";

    private T messageInfo;

}
