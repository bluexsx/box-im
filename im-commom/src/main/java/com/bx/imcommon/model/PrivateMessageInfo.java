package com.bx.imcommon.model;

import com.bx.imcommon.serializer.DateToLongSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.util.Date;

@Data
public class PrivateMessageInfo {

    private long id;

    private Long sendId;

    private Long recvId;

    private String content;

    private Integer type;

    @JsonSerialize(using = DateToLongSerializer.class)
    private Date sendTime;
}
