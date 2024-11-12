package com.bx.implatform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "邀请好友进群请求VO")
public class GroupInviteVO {

    @NotNull(message = "群id不可为空")
    @Schema(description = "群id")
    private Long groupId;

    @NotEmpty(message = "群id不可为空")
    @Schema(description = "好友id列表不可为空")
    private List<Long> friendIds;
}
