package com.bx.imcommon.enums;


public enum IMSendCode {

    SUCCESS(0,"发送成功"),
    NOT_ONLINE(1,"对方当前不在线"),
    NOT_FIND_CHANNEL(2,"未找到对方的channel"),
    UNKONW_ERROR(9999,"未知异常");

    private Integer code;
    private String desc;

    // 构造方法
    IMSendCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static IMSendCode fromCode(Integer code){
        for (IMSendCode typeEnum:values()) {
            if (typeEnum.code.equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }


    public String description() {
        return desc;
    }


    public Integer code(){
        return this.code;
    }

}
