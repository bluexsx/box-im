package com.lx.implatform.exception;

import cn.hutool.json.JSONException;
import com.lx.common.enums.ResultCode;
import com.lx.common.result.Result;
import com.lx.common.result.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.lang.reflect.UndeclaredThrowableException;
import java.time.format.DateTimeParseException;
import java.util.List;


@ControllerAdvice
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public Result handleException(Exception e){
        if(e instanceof GlobalException) {
            GlobalException ex = (GlobalException)e;
            log.error("全局异常捕获:msg:{},log:{},{}" , ex.getMessage(), e);
            return ResultUtils.error(ex.getCode(), ex.getMessage());
        }
        else if(e instanceof UndeclaredThrowableException) {
            GlobalException ex = (GlobalException) e.getCause();
            log.error("全局异常捕获:msg:{},log:{},{}" , ex.getMessage(), e);
            return ResultUtils.error(ex.getCode(), ex.getMessage());
        }else{
            log.error("全局异常捕获:msg:{},{}" , e.getMessage(), e);
            return ResultUtils.error(ResultCode.PROGRAM_ERROR);
        }
    }


    /**
     *  数据解析错误
     **/
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public Result handleMessageNotReadableException(HttpMessageNotReadableException e){
        log.error("全局异常捕获:msg:{}" , e.getMessage());
        Throwable t = e.getCause();
        if(t instanceof JSONException){
            t = t.getCause();
            if(t instanceof DateTimeParseException){
                return ResultUtils.error(ResultCode.PROGRAM_ERROR, "日期格式不正确");
            }
            return ResultUtils.error(ResultCode.PROGRAM_ERROR,  "数据格式不正确");
        }
        return ResultUtils.error(ResultCode.PROGRAM_ERROR);
    }

    /**
     * 处理请求参数格式错误 @RequestBody上validate失败后抛出的异常
     * @param exception
     * @return
     */
    @ExceptionHandler(value = { MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.OK)
    public Result handleValidationExceptionHandler(MethodArgumentNotValidException exception) {
        BindingResult bindResult = exception.getBindingResult();
        String msg;
        if (bindResult != null && bindResult.hasErrors()) {
            msg = bindResult.getAllErrors().get(0).getDefaultMessage();
            if (msg.contains("NumberFormatException")) {
                msg = "参数类型错误！";
            }
        }else {
            msg = "系统繁忙，请稍后重试...";
        }
        return ResultUtils.error(ResultCode.PROGRAM_ERROR,msg);
    }
    

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result handleBindException(BindException e){
        //抛出异常可能不止一个 输出为一个List集合
        List<ObjectError> errors = e.getAllErrors();
        //取第一个异常
        ObjectError error = errors.get(0);
        //获取异常信息
        String errorMsg = error.getDefaultMessage();
        return ResultUtils.error(ResultCode.PROGRAM_ERROR,errorMsg);
    }

}
