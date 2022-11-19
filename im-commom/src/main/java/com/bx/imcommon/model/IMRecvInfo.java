package com.bx.imcommon.model;

import lombok.Data;

import java.util.List;

@Data
public class IMRecvInfo<T> {

    private Integer cmd;

    private List<Long> recvIds;

    private T data;
}


