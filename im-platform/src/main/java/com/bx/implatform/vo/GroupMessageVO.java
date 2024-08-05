package com.bx.implatform.vo;

import com.bx.imcommon.serializer.DateToLongSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class GroupMessageVO {

    @ApiModelProperty(value = "消息id")
    private Long id;

    @ApiModelProperty(value = "群聊id")
    private Long groupId;

    @ApiModelProperty(value = " 发送者id")
    private Long sendId;

    @ApiModelProperty(value = " 发送者昵称")
    private String sendNickName;

    @ApiModelProperty(value = "消息内容")
    private String content;

    @ApiModelProperty(value = "消息内容类型 具体枚举值由应用层定义")
    private Integer type;

    @ApiModelProperty(value = "是否回执消息")
    private Boolean receipt;

    @ApiModelProperty(value = "回执消息是否完成")
    private Boolean receiptOk;

    @ApiModelProperty(value = "已读消息数量")
    private Integer readedCount = 0;

    @ApiModelProperty(value = "@用户列表")
    private List<Long> atUserIds;

    @ApiModelProperty(value = " 状态")
    private Integer status;

    @ApiModelProperty(value = "发送时间")
    @JsonSerialize(using = DateToLongSerializer.class)
    private Date sendTime;
}
