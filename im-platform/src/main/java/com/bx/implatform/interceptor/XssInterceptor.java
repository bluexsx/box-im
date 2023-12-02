package com.bx.implatform.interceptor;

import com.bx.implatform.enums.ResultCode;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.util.XssUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.util.Map;

@Slf4j
@Component
public class XssInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
        // 检查参数
        Map<String, String[]> paramMap = request.getParameterMap();
        for (String[] values : paramMap.values()) {
            for (String value : values) {
                if (XssUtil.checkXss(value)) {
                    throw new GlobalException(ResultCode.XSS_PARAM_ERROR);
                }
            }
        }
        //  检查body
        String body = getBody(request);
        if (XssUtil.checkXss(body)) {
            throw new GlobalException(ResultCode.XSS_PARAM_ERROR);
        }
        return true;
    }

    @SneakyThrows
    private String getBody(HttpServletRequest request) {
        BufferedReader reader = request.getReader();
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        return stringBuilder.toString();
    }
}
