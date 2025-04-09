package com.bx.imcommon.enums;

import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public enum IMTerminalType {

    /**
     * web
     */
    WEB(0, "web"),
    /**
     * app
     */
    APP(1, "app"),
    /**
     * pc
     */
    PC(2, "pc"),
    /**
     * 未知
     */
    UNKNOW(-1, "未知");

    private final Integer code;

    private final String desc;


    public static IMTerminalType fromCode(Integer code) {
        for (IMTerminalType typeEnum : values()) {
            if (typeEnum.code.equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }

    public static List<Integer> codes() {
        return Arrays.stream(values()).map(IMTerminalType::code).collect(Collectors.toList());
    }

    public Integer code() {
        return this.code;
    }

}
