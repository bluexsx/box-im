package com.bx.implatform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: 谢绍许
 * @date: 2024-06-01
 * @version: 1.0
 */
@Getter
@AllArgsConstructor
public enum WebrtcMode {

    /**
     * 视频通话
     */
    VIDEO( "video"),

    /**
     * 语音通话
     */
    VOICE( "voice");

    private final String value;

}
