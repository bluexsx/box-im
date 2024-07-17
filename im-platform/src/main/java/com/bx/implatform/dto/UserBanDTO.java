package com.bx.implatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author: Blue
 * @date: 2024-07-14
 * @version: 1.0
 */
@Data
@Schema(description = "用户锁定DTO")
public class UserBanDTO {

    @Schema(description = "用户id")
    private Long id;

    @Schema(description = "锁定原因")
    private String reason;

}
