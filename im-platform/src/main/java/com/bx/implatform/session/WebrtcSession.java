package com.bx.implatform.session;

import com.bx.imcommon.enums.IMTerminalType;
import io.swagger.models.auth.In;
import lombok.Data;

/*
 * webrtc 会话信息
 * @Author Blue
 * @Date 2022/10/21
 */
@Data
public class WebrtcSession {
    /**
     * 发起者id
     */
    private Long callerId;
    /**
     * 发起者终端类型
     */
    private Integer callerTerminal;

    /**
     * 接受者id
     */
    private Long acceptorId;

    /**
     * 接受者终端类型
     */
    private Integer acceptorTerminal;
}
