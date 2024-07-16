package com.bx.implatform.vo;

import com.bx.imcommon.serializer.DateToLongSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("系统消息VO")
public class SystemMessageVO {

    @ApiModelProperty(value = " 消息id")
    private Long id;

    @ApiModelProperty(value = " 发送内容")
    private String content;

    @ApiModelProperty(value = "消息内容类型 MessageType")
    private Integer type;

    @ApiModelProperty(value = " 发送时间")
    @JsonSerialize(using = DateToLongSerializer.class)
    private Date sendTime;
}
