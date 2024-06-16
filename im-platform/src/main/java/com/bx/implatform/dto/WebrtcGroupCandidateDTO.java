package com.bx.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author: 谢绍许
 * @date: 2024-06-01
 * @version: 1.0
 */
@Data
@ApiModel("发起群视频通话DTO")
public class WebrtcGroupCandidateDTO {

    @NotNull(message = "群聊id不可为空")
    @ApiModelProperty(value = "群聊id")
    private Long groupId;

    @NotNull(message = "用户id不可为空")
    @ApiModelProperty(value = "用户id")
    private Long userId;

    @NotEmpty(message = "candidate信息不可为空")
    @ApiModelProperty(value = "candidate信息")
    private String candidate;

}
