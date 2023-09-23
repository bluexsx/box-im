package com.bx.imcommon.model;

import com.bx.imcommon.enums.IMSendCode;
import com.bx.imcommon.enums.IMTerminalType;
import lombok.Data;

@Data
public class SendResult<T> {

    /*
     * 接收者id
     */
    private Long recvId;

    /*
     * 接收者终端类型 IMTerminalType
     */
    private Integer recvTerminal;

    /*
     * 发送状态 IMCmdType
     */
    private Integer code;

    /*
     *  消息内容
     */
    private T data;

}
