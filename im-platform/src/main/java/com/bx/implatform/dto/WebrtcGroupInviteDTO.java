package com.bx.implatform.dto;

import com.bx.implatform.session.WebrtcUserInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @author: Blue
 * @date: 2024-06-01
 * @version: 1.0
 */
@Data
@Schema(description = "邀请用户进入群视频通话DTO")
public class WebrtcGroupInviteDTO {

    @NotNull(message = "群聊id不可为空")
    @Schema(description = "群聊id")
    private Long groupId;

    @NotEmpty(message = "参与用户信息不可为空")
    @Schema(description = "参与用户信息")
    private List<WebrtcUserInfo> userInfos;

}
