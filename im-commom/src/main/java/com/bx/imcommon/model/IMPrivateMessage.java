package com.bx.imcommon.model;

import com.bx.imcommon.enums.IMTerminalType;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;


@Data
public class IMPrivateMessage<T> {

    /**
     * 发送者id
     */
    private Long sendId;

    /**
     * 发送者终端类型 IMTerminalType
     */
    private Integer sendTerminal;

    /**
     * 是否发送给自己的其他终端
     */
    private Boolean sendToSelf ;

    /**
     * 接收者id
     */
    private Long recvId;

    /**
     *  消息内容(可一次性发送多条)
     */
    private List<T> datas;


}
