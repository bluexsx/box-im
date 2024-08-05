package com.bx.implatform.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author: Blue
 * @date: 2023-10-28 21:17:59
 * @version: 1.0
 */
@Data
@AllArgsConstructor
public class OnlineTerminalVO {

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "在线终端类型")
    private List<Integer> terminals;

}
