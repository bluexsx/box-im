package com.bx.implatform.session;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author: 谢绍许
 * @date: 2024-06-02
 * @version: 1.0
 */
@Data
@ApiModel("用户信息")
public class WebrtcUserInfo {
    @ApiModelProperty(value = "用户id")
    private Long id;

    @ApiModelProperty(value = "用户昵称")
    private String nickName;

    @ApiModelProperty(value = "用户头像")
    private String headImage;

    @ApiModelProperty(value = "是否开启摄像头")
    private Boolean isCamera;

    @ApiModelProperty(value = "是否开启麦克风")
    private Boolean isMicroPhone;
}
