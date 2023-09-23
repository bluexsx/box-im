package com.bx.imcommon.model;

import lombok.Data;

import java.util.List;

@Data
public class IMRecvInfo {

    /*
     * 命令类型 IMCmdType
     */
    private Integer cmd;

    /*
     * 发送者id
     */
    private Long sendId;

    /*
     * 接收终端类型 IMTerminalType
     */
    private Integer recvTerminal;

    /*
     * 接收者id列表
     */
    private List<Long> recvIds;

    /*
     *  是否需要回调发送结果
     */
    private Boolean sendResult;

    /*
     * 推送消息体
     */
    private Object data;
}


