package com.bx.implatform.aspect;

import com.bx.imclient.IMClient;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.session.UserSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @author: blue
 * @date: 2024-06-16
 * @version: 1.0
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OnlineCheckAspect {

    private final IMClient imClient;

    @Around("@annotation(com.bx.implatform.annotation.OnlineCheck)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        UserSession session = SessionContext.getSession();
        if(!imClient.isOnline(session.getUserId())){
            throw new GlobalException("您当前的网络连接已断开,请稍后重试");
        }
        return joinPoint.proceed();
    }

}
