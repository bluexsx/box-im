package com.lx.implatform.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("单发消息VO")
public class SingleMessageVO {


    @NotNull(message="接收用户id不可为空")
    @ApiModelProperty(value = "接收用户id")
    private Long recvUserId;


    @NotEmpty(message="发送内容不可为空")
    @ApiModelProperty(value = "发送内容")
    private String content;

    @NotNull(message="发送内容不可为空")
    @ApiModelProperty(value = "消息类型")
    private Integer type;

}
