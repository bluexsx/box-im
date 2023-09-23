package com.bx.imcommon.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum IMTerminalType {

    WEB(0,"web"),
    APP(1,"app");

    private Integer code;

    private String desc;

    IMTerminalType(Integer index, String desc) {
        this.code =index;
        this.desc=desc;
    }

    public static IMTerminalType fromCode(Integer code){
        for (IMTerminalType typeEnum:values()) {
            if (typeEnum.code.equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }

    public static List<Integer> codes(){
        return Arrays.stream(values()).map(IMTerminalType::code).collect(Collectors.toList());
    }

    public String description() {
        return desc;
    }

    public Integer code(){
        return this.code;
    }

}
