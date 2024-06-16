package com.bx.implatform.dto;

import com.bx.implatform.session.WebrtcUserInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author: Blue
 * @date: 2024-06-01
 * @version: 1.0
 */
@Data
@ApiModel("邀请用户进入群视频通话DTO")
public class WebrtcGroupInviteDTO {

    @NotNull(message = "群聊id不可为空")
    @ApiModelProperty(value = "群聊id")
    private Long groupId;

    @NotEmpty(message = "参与用户信息不可为空")
    @ApiModelProperty(value = "参与用户信息")
    private List<WebrtcUserInfo> userInfos;

}
