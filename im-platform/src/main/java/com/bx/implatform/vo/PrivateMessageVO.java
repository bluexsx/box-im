package com.bx.implatform.vo;

import com.bx.imcommon.serializer.DateToLongSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("私聊消息VO")
public class PrivateMessageVO {

    @ApiModelProperty(value = " 消息id")
    private long id;

    @ApiModelProperty(value = " 发送者id")
    private Long sendId;

    @ApiModelProperty(value = " 接收者id")
    private Long recvId;

    @ApiModelProperty(value = " 发送内容")
    private String content;

    @ApiModelProperty(value = "消息内容类型 IMCmdType")
    private Integer type;

    @ApiModelProperty(value = " 状态")
    private Integer status;

    @ApiModelProperty(value = " 发送时间")
    @JsonSerialize(using = DateToLongSerializer.class)
    private Date sendTime;
}
