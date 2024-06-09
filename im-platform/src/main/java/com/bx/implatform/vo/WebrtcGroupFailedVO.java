package com.bx.implatform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author: 谢绍许
 * @date: 2024-06-09
 * @version: 1.0
 */
@Data
@ApiModel("用户加入群通话失败VO")
public class WebrtcGroupFailedVO {

    @ApiModelProperty(value = "失败用户列表")
    private List<Long> userIds;

    @ApiModelProperty(value = "失败原因")
    private String reason;
}
