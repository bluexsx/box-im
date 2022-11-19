package com.bx.implatform.exception;

import com.bx.implatform.enums.ResultCode;
import lombok.Data;

import java.io.Serializable;


@Data
public class GlobalException extends RuntimeException implements Serializable {
    private static final long serialVersionUID = 8134030011662574394L;
    private Integer code;
    private String message;

    public GlobalException(Integer code, String message){
        this.code=code;
        this.message=message;
    }

    public GlobalException(ResultCode resultCode, String message){
        this.code = resultCode.getCode();
        this.message=message;
    }

    public GlobalException(ResultCode resultCode) {
        this.code = resultCode.getCode();
        this.message = resultCode.getMsg();
    }

    public GlobalException(String message){
        this.code= ResultCode.PROGRAM_ERROR.getCode();
        this.message=message;
    }

}
