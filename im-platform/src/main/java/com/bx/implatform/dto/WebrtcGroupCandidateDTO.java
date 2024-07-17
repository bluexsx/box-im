package com.bx.implatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author: Blue
 * @date: 2024-06-01
 * @version: 1.0
 */
@Data
@Schema(description = "发起群视频通话DTO")
public class WebrtcGroupCandidateDTO {

    @NotNull(message = "群聊id不可为空")
    @Schema(description = "群聊id")
    private Long groupId;

    @NotNull(message = "用户id不可为空")
    @Schema(description = "用户id")
    private Long userId;

    @NotEmpty(message = "candidate信息不可为空")
    @Schema(description = "candidate信息")
    private String candidate;

}
