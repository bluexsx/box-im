package com.bx.implatform.session;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/*
 * @Description
 * @Author Blue
 * @Date 2022/10/21
 */
public class SessionContext {

    public static UserSession getSession() {
        // 从请求上下文里获取Request对象
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        return (UserSession) request.getAttribute("session");
    }

}
