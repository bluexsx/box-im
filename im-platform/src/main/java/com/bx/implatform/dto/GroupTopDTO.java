package com.bx.implatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Blue
 * @version 1.0
 */
@Data
@Schema(description = "会话置顶状态设置")
public class GroupTopDTO {

    @NotNull(message = "群id不可为空")
    @Schema(description = "群组id")
    private Long groupId;

    @NotNull(message = "置顶状态不可为空")
    @Schema(description = "置顶状态")
    private Boolean isTop;

}
