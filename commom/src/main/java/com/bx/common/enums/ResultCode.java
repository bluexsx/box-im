package com.bx.common.enums;

/**
 * 响应码枚举
 *
 * @author Blue
 * @date 2020/10/19
 *
 **/
public enum ResultCode {
    SUCCESS(200,"成功"),
    LOGIN_ERROR(400,"登录异常"),
    NO_LOGIN(401,"未登录"),
    FORBIDDEN(403,"禁止访问"),
    NOT_FIND(404,"无法找到文件"),
    PROGRAM_ERROR(500,"系统繁忙，请稍后再试"),
    PASSWOR_ERROR(10001,"密码不正确"),
    VERITY_CODE_NOT_EXIST(10002,"验证码不存在"),
    VERITY_CODE_ERROR(10003,"验证码不正确"),
    USERNAME_ALREADY_REGISTER(10004,"该用户名已注册"),
    MOBILE_ALREADY_REGISTER(10005,"该手机号码已注册"),
    ;


    private int code;
    private String msg;

    // 构造方法
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

