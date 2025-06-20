package com.bx.implatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Blue
 * @version 1.0
 * @date 2025-02-23
 */
@Data
@Schema(description = "群聊免打扰")
public class GroupDndDTO {

    @NotNull(message = "群id不可为空")
    @Schema(description = "群组id")
    private Long groupId;

    @NotNull(message = "免打扰状态不可为空")
    @Schema(description = "免打扰状态")
    private Boolean isDnd;
}
