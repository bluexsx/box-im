package com.bx.implatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Blue
 * @version 1.0
 */
@Data
@Schema(description = "好友会话置顶")
public class FriendTopDTO {

    @NotNull(message = "好友id不可为空")
    @Schema(description = "好友用户id")
    private Long friendId;

    @NotNull(message = "置顶状态不可为空")
    @Schema(description = "置顶状态")
    private Boolean isTop;

}
