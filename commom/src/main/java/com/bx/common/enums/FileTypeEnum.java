package com.bx.common.enums;

public enum FileTypeEnum {

    FILE(0,"文件"),
    IMAGE(1,"图片"),
    VIDEO(2,"视频"),
    AUDIO(3,"声音");



    private Integer code;

    private String desc;

    FileTypeEnum(Integer index, String desc) {
        this.code =index;
        this.desc=desc;
    }

    public static FileTypeEnum fromCode(Integer code){
        for (FileTypeEnum typeEnum:values()) {
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

