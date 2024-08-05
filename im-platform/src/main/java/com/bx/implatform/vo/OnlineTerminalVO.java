package com.bx.implatform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "用户id")
    private Long userId;

    @Schema(description = "在线终端类型")
    private List<Integer> terminals;

}
