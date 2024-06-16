package com.bx.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author: 谢绍许
 * @date: 2024-06-01
 * @version: 1.0
 */
@Data
@ApiModel("用户设备操作DTO")
public class WebrtcGroupDeviceDTO {

    @NotNull(message = "群聊id不可为空")
    @ApiModelProperty(value = "群聊id")
    private Long groupId;

    @ApiModelProperty(value = "是否开启摄像头")
    private Boolean isCamera;

    @ApiModelProperty(value = "是否开启麦克风")
    private Boolean isMicroPhone;

}
