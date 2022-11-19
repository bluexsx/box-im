package com.bx.common.model.im;

import com.bx.common.enums.SendResultType;
import lombok.Data;

@Data
public class SendResult<T> {

    private Long recvId;

    private SendResultType result;

    private String failReason="";

    private T messageInfo;

}
