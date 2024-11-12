package com.bx.implatform.vo;

import com.bx.implatform.session.WebrtcUserInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author: Blue
 * @date: 2024-06-09
 * @version: 1.0
 */
@Data
@Schema(description = "群通话信息VO")
public class WebrtcGroupInfoVO {


    @Schema(description = "是否在通话中")
    private Boolean isChating;

    @Schema(description = "通话发起人")
    WebrtcUserInfo host;

    @Schema(description = "通话用户列表")
    private List<WebrtcUserInfo> userInfos;
}
