package com.bx.imcommon.model;

import lombok.Data;

import java.util.List;

@Data
public class IMRecvInfo {

    /**
     * 命令类型 IMCmdType
     */
    private Integer cmd;

    /**
     * 发送方
     */
    private IMUserInfo sender;

    /**
     * 接收方用户列表
     */
    List<IMUserInfo> receivers;

    /**
     *  是否需要回调发送结果
     */
    private Boolean sendResult;

    /**
     * 推送消息体
     */
    private Object data;
}


