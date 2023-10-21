package com.bx.implatform.enums;

public enum FileType {

    FILE(0,"文件"),
    IMAGE(1,"图片"),
    VIDEO(2,"视频"),
    AUDIO(3,"声音");



    private final Integer code;

    private final String desc;

    FileType(Integer index, String desc) {
        this.code =index;
        this.desc=desc;
    }


    public String description() {
        return desc;
    }

    public Integer code(){
        return this.code;
    }


}

