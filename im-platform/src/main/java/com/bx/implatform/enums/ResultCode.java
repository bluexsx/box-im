package com.bx.implatform.enums;

/**
 * 响应码枚举
 *
 * @author Blue
 * @date 2020/10/19
 *
 **/
public enum ResultCode {
    SUCCESS(200,"成功"),
    NO_LOGIN(400,"未登录"),
    INVALID_TOKEN(401,"token无效或已过期"),
    PROGRAM_ERROR(500,"系统繁忙，请稍后再试"),
    PASSWOR_ERROR(10001,"密码不正确"),
    USERNAME_ALREADY_REGISTER(10003,"该用户名已注册"),
    XSS_PARAM_ERROR(10004,"请不要输入非法内容"),
    ;


    private int code;
    private String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}

