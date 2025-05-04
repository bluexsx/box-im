package com.bx.implatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "邀请好友进群请求DTO")
public class GroupInviteDTO {

    @NotNull(message = "群id不可为空")
    @Schema(description = "群id")
    private Long groupId;

    @Size(max = 50, message = "一次最多只能邀请50位用户")
    @NotEmpty(message = "群id不可为空")
    @Schema(description = "好友id列表不可为空")
    private List<Long> friendIds;
}
