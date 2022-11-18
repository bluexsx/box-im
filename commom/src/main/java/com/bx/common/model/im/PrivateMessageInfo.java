package com.bx.common.model.im;

import com.bx.common.serializer.DateToLongSerializer;
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
