package com.bx.imcommon.enums;


public enum IMSendStatus  {

    SUCCESS(0,"发送成功"),
    FAIL(1,"发送失败");

    private int code;
    private String desc;

    // 构造方法
    IMSendStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String description() {
        return desc;
    }


    public Integer code(){
        return this.code;
    }

}
