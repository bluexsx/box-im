package com.bx.imcommon.model;

import com.bx.imcommon.serializer.DateToLongSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.util.Date;

@Data
public class PrivateMessageInfo {

    /*
     * 消息id
     */
    private long id;

    /*
     * 发送者id
     */
    private Long sendId;

    /*
     * 接收者id
     */
    private Long recvId;

    /*
     * 发送内容
     */
    private String content;

    /*
     * 消息内容类型 具体枚举值由应用层定义
     */
    private Integer type;

    /**
     * 发送时间
     */
    @JsonSerialize(using = DateToLongSerializer.class)
    private Date sendTime;
}
