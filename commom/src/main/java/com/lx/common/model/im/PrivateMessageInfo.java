package com.lx.common.model.im;

import lombok.Data;

import java.util.Date;

@Data
public class PrivateMessageInfo {

    private long id;

    private Long sendId;

    private Long recvId;

    private String content;

    private Integer type;

    private Date sendTime;
}
