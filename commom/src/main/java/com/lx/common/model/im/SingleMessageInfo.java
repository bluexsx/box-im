package com.lx.common.model.im;

import lombok.Data;

import java.util.Date;

@Data
public class SingleMessageInfo {

    private long id;

    private Long sendUserId;

    private Long recvUserId;

    private String content;

    private Integer type;

    private Date sendTime;
}
