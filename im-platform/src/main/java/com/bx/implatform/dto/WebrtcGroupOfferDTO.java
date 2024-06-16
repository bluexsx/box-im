package com.bx.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author: Blue
 * @date: 2024-06-01
 * @version: 1.0
 */
@Data
@ApiModel("回复用户连接请求DTO")
public class WebrtcGroupOfferDTO {

    @NotNull(message = "群聊id不可为空")
    @ApiModelProperty(value = "群聊id")
    private Long groupId;

    @NotNull(message = "用户id不可为空")
    @ApiModelProperty(value = "用户id,代表回复谁的连接请求")
    private Long userId;

    @NotEmpty(message = "offer不可为空")
    @ApiModelProperty(value = "用户offer信息")
    private String offer;

}
