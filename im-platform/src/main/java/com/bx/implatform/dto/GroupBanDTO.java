package com.bx.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author: Blue
 * @date: 2024-07-14
 * @version: 1.0
 */
@Data
@ApiModel(description = "群组封禁")
public class GroupBanDTO {

    @ApiModelProperty(value = "群组id")
    private Long id;

    @ApiModelProperty(value = "封禁原因")
    private String reason;
}
