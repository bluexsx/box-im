package com.bx.implatform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("群聊消息VO")
public class GroupMessageVO {

    @NotNull(message="群聊id不可为空")
    @ApiModelProperty(value = "群聊id")
    private Long groupId;


    @Length(max=1024,message = "内容长度不得大于1024")
    @NotEmpty(message="发送内容不可为空")
    @ApiModelProperty(value = "发送内容")
    private String content;

    @NotNull(message="发送内容不可为空")
    @ApiModelProperty(value = "消息类型")
    private Integer type;
}
