package com.bx.implatform.result;

import com.bx.implatform.enums.ResultCode;

public final class ResultUtils {

    private ResultUtils() {
    }

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMsg());
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMsg());
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(T data, String messsage) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(messsage);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(String messsage) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(messsage);
        return result;
    }

    public static <T> Result<T> error(Integer code, String messsage) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(messsage);
        return result;
    }


    public static <T> Result<T> error(ResultCode resultCode, String messsage) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMessage(messsage);
        return result;
    }

    public static <T> Result<T> error(ResultCode resultCode) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMessage(resultCode.getMsg());
        return result;
    }


}
