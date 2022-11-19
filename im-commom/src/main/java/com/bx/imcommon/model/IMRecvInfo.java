package com.bx.imcommon.model;

import lombok.Data;

import java.util.List;

@Data
public class IMRecvInfo<T> {

    /*
     * 命令类型
     */
    private Integer cmd;

    /*
     * 接收者id列表
     */
    private List<Long> recvIds;

    /*
     * 推送消息体
     */
    private T data;
}


