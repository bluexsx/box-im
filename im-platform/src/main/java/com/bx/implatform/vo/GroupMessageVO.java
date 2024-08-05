package com.bx.implatform.vo;

import com.bx.imcommon.serializer.DateToLongSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class GroupMessageVO {

    @Schema(description = "消息id")
    private Long id;

    @Schema(description = "群聊id")
    private Long groupId;

    @Schema(description = " 发送者id")
    private Long sendId;

    @Schema(description = " 发送者昵称")
    private String sendNickName;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "消息内容类型 具体枚举值由应用层定义")
    private Integer type;

    @Schema(description = "是否回执消息")
    private Boolean receipt;

    @Schema(description = "回执消息是否完成")
    private Boolean receiptOk;

    @Schema(description = "已读消息数量")
    private Integer readedCount = 0;

    @Schema(description = "@用户列表")
    private List<Long> atUserIds;

    @Schema(description = " 状态")
    private Integer status;

    @Schema(description = "发送时间")
    @JsonSerialize(using = DateToLongSerializer.class)
    private Date sendTime;
}
