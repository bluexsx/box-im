package com.bx.implatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * @author Blue
 * @version 1.0
 * @date 2025-02-23
 */
@Data
@Schema(description = "移除群聊成员")
public class GroupMemberRemoveDTO {

    @NotNull(message = "群id不可为空")
    @Schema(description = "群组id")
    private Long groupId;

    @Size(max = 50, message = "一次最多只能选择50位用户")
    @NotEmpty(message = "成员用户id不可为空")
    @Schema(description = "成员用户id")
    private List<Long> userIds;
}
