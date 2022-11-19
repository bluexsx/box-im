package com.bx.imcommon.model.im;

import com.bx.imcommon.enums.SendResultType;
import lombok.Data;

@Data
public class SendResult<T> {

    private Long recvId;

    private SendResultType result;

    private String failReason="";

    private T messageInfo;

}
