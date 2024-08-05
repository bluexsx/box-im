package com.bx.implatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author: Blue
 * @date: 2024-06-01
 * @version: 1.0
 */
@Data
@Schema(description = "用户通话失败DTO")
public class WebrtcGroupFailedDTO {

    @NotNull(message = "群聊id不可为空")
    @Schema(description = "群聊id")
    private Long groupId;

    @Schema(description = "失败原因")
    private String reason;

}
