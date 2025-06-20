package com.bx.implatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Blue
 * @version 1.0
 */
@Data
@Schema(description = "好友免打扰")
public class FriendDndDTO {

    @NotNull(message = "好友id不可为空")
    @Schema(description = "好友用户id")
    private Long friendId;

    @NotNull(message = "消息免打扰状态不可为空")
    @Schema(description = "消息免打扰状态")
    private Boolean isDnd;

}
