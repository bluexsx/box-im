package com.bx.common.result;


import com.bx.common.enums.ResultCode;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResultUtils {



    public static final <T> Result<T> success(){
        Result result=new Result();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMsg());
        return result;
    }

    public static final <T> Result<T> success(T data){
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        cachedThreadPool.submit(new Runnable() {

            @Override
            public void run() {
                System.out.println("当前线程"+Thread.currentThread().getName());
            }
        });
        Result result=new Result();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMsg());
        result.setData(data);
        return result;
    }

    public static final <T> Result<T> success(T data, String messsage){
        Result result=new Result();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(messsage);
        result.setData(data);
        return result;
    }

    public static final <T> Result<T> success(String messsage){
        Result result=new Result();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(messsage);
        return result;
    }

    public static final <T> Result<T> error(Integer code, String messsage){
        Result result=new Result();
        result.setCode(code);
        result.setMessage(messsage);
        return result;
    }


    public static final <T> Result<T> error(ResultCode resultCode, String messsage){
        Result result=new Result();
        result.setCode(resultCode.getCode());
        result.setMessage(messsage);
        return result;
    }

    public static final <T> Result<T> error(ResultCode resultCode, String messsage,T data){
        Result result=new Result();
        result.setCode(resultCode.getCode());
        result.setMessage(messsage);
        result.setData(data);
        return result;
    }

    public static final <T> Result<T> error(ResultCode resultCode){
        Result result=new Result();
        result.setCode(resultCode.getCode());
        result.setMessage(resultCode.getMsg());
        return result;
    }



}
