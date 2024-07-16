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
@ApiModel(description = "群组解锁")
public class GroupUnbanDTO {

    @ApiModelProperty(value = "群组id")
    private Long id;

}
