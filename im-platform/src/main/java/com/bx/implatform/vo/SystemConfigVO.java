package com.bx.implatform.vo;

import com.bx.implatform.config.WebrtcConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author: blue
 * @date: 2024-06-10
 * @version: 1.0
 */
@Data
@Schema(description = "系统配置VO")
@AllArgsConstructor
public class SystemConfigVO {

    @Schema(description = "webrtc配置")
    private WebrtcConfig webrtc;

}
