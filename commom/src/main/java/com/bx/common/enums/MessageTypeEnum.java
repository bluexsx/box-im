package com.bx.common.enums;


public enum MessageTypeEnum {

    TEXT(0,"文字"),
    FILE(1,"文件"),
    IMAGE(2,"图片"),
    VIDEO(3,"视频"),
    TIP(10,"系统提示");

    private Integer code;

    private String desc;

    MessageTypeEnum(Integer index, String desc) {
        this.code =index;
        this.desc=desc;
    }

    public static MessageTypeEnum fromCode(Integer code){
        for (MessageTypeEnum typeEnum:values()) {
            if (typeEnum.code.equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }


    public String getDesc() {
        return desc;
    }

    public Integer getCode(){
        return this.code;
    }
}
