package com.bx.implatform.session;

import lombok.Data;

/*
 * webrtc 会话信息
 * @Author Blue
 * @Date 2022/10/21
 */
@Data
public class WebrtcPrivateSession {
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

    /**
     *  通话模式
     */
    private String mode;
    /**
     * 开始聊天时间戳
     */
    private Long  chatTimeStamp;
}
