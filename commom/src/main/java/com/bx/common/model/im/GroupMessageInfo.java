package com.bx.common.model.im;

import com.bx.common.serializer.DateToLongSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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

    @JsonSerialize(using = DateToLongSerializer.class)
    private Date sendTime;
}
