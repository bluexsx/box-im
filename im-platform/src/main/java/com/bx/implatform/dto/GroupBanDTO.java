package com.bx.implatform.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author: Blue
 * @date: 2024-07-14
 * @version: 1.0
 */
@Data
@Schema(description = "群组封禁")
public class GroupBanDTO {

    @Schema(description = "群组id")
    private Long id;

    @Schema(description = "封禁原因")
    private String reason;
}
