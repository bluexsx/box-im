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
@Schema(description = "回复用户连接请求DTO")
public class WebrtcGroupAnswerDTO {

    @NotNull(message = "群聊id不可为空")
    @Schema(description = "群聊id")
    private Long groupId;

    @NotNull(message = "用户id不可为空")
    @Schema(description = "用户id,代表回复谁的连接请求")
    private Long userId;

    @NotEmpty(message = "anwer不可为空")
    @Schema(description = "用户本地anwer信息")
    private String answer;

}
