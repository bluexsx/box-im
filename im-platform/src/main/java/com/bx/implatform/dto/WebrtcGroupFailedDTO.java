package com.bx.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author: 谢绍许
 * @date: 2024-06-01
 * @version: 1.0
 */
@Data
@ApiModel("用户通话失败DTO")
public class WebrtcGroupFailedDTO {

    @NotNull(message = "群聊id不可为空")
    @ApiModelProperty(value = "群聊id")
    private Long groupId;

    @ApiModelProperty(value = "失败原因")
    private String reason;

}
