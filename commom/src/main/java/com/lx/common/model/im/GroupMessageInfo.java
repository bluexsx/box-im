package com.lx.common.model.im;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class GroupMessageInfo {

    private Long id;

    private Long groupId;

    private Long sendId;

    private List<Long> recvIds;

    private String content;

    private Integer type;

    private Date sendTime;
}
