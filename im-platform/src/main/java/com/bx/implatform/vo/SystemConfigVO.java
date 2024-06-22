package com.bx.implatform.vo;

import com.bx.implatform.config.WebrtcConfig;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author: blue
 * @date: 2024-06-10
 * @version: 1.0
 */
@Data
@ApiModel("系统配置VO")
@AllArgsConstructor
public class SystemConfigVO {

    @ApiModelProperty(value = "webrtc配置")
    private WebrtcConfig webrtc;

}
