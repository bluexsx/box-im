package com.bx.implatform.aspect;

import cn.hutool.core.util.StrUtil;
import com.bx.implatform.annotation.RedisLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.Redisson;
import org.redisson.RedissonLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author: blue
 * @date: 2024-06-09
 * @version: 1.0
 */

@Slf4j
@Aspect
@Order(0)
@Component
@RequiredArgsConstructor
public class RedisLockAspect {

    private ExpressionParser parser = new SpelExpressionParser();
    private DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    private final RedissonClient redissonClient;

    @Around("@annotation(com.bx.implatform.annotation.RedisLock)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RedisLock annotation = method.getAnnotation(RedisLock.class);
        // 解析表达式中的key
        String key = parseKey(joinPoint);
        String lockKey = StrUtil.join(":",annotation.prefixKey(),key);
        // 上锁
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock(annotation.waitTime(),annotation.unit());
        try {
            // 执行方法
            return joinPoint.proceed();
        }finally {
            lock.unlock();
        }
    }

    private String parseKey(ProceedingJoinPoint joinPoint){
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RedisLock annotation = method.getAnnotation(RedisLock.class);
        // el解析需要的上下文对象
        EvaluationContext context = new StandardEvaluationContext();
        // 参数名
        String[] params = parameterNameDiscoverer.getParameterNames(method);
        if(Objects.isNull(params)){
            return annotation.key();
        }
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < params.length; i++) {
            context.setVariable(params[i], args[i]);//所有参数都作为原材料扔进去
        }
        Expression expression = parser.parseExpression(annotation.key());
        return expression.getValue(context, String.class);
    }


}
