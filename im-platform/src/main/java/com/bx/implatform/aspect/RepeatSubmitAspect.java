package com.bx.implatform.aspect;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSON;
import com.bx.implatform.annotation.RepeatSubmit;
import com.bx.implatform.contant.RedisKey;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.session.SessionContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.Map;
import java.util.StringJoiner;

/**
 * @author: blue
 * @date: 2024-12-08
 * @version: 1.0
 */

@Aspect
@Component
@AllArgsConstructor
public class RepeatSubmitAspect {

    private final RedisTemplate<String, Object> redisTemplate;

    @Before("@annotation(repeatSubmit)")
    public void doBefore(JoinPoint point, RepeatSubmit repeatSubmit) throws Throwable {
        // 如果注解不为0 则使用注解数值
        long interval = repeatSubmit.timeUnit().toMillis(repeatSubmit.interval());
        HttpServletRequest request =
            ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
        String url = request.getRequestURL().toString();
        Long userId = SessionContext.getSession().getUserId();
        String reqParams = argsArrayToString(point.getArgs());
        String md5 = SecureUtil.md5(StrUtil.join(":", userId, url, reqParams));
        // 唯一标识
        String key = String.join(":",RedisKey.IM_REPEAT_SUBMIT,md5) ;
        if(redisTemplate.hasKey(key)){
            throw new GlobalException(repeatSubmit.message());
        }
        redisTemplate.opsForValue().set(key,1,repeatSubmit.interval(),repeatSubmit.timeUnit());
    }


    /**
     * 参数拼装
     */
    private String argsArrayToString(Object[] paramsArray) {
        StringJoiner params = new StringJoiner(" ");
        if (ArrayUtil.isEmpty(paramsArray)) {
            return params.toString();
        }
        for (Object o : paramsArray) {
            if (ObjectUtil.isNotNull(o) && !isFilterObject(o)) {
                params.add(JSON.toJSONString(o));
            }
        }
        return params.toString();
    }

    /**
     * 判断是否需要过滤的对象。
     *
     * @param o 对象信息。
     * @return 如果是需要过滤的对象，则返回true；否则返回false。
     */
    @SuppressWarnings("rawtypes")
    public boolean isFilterObject(final Object o) {
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Collection collection = (Collection)o;
            for (Object value : collection) {
                return value instanceof MultipartFile;
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map map = (Map)o;
            for (Object value : map.values()) {
                return value instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse;
    }
}
