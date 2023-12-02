package com.bx.implatform.util;

import org.springframework.util.ReflectionUtils;

public final class BeanUtils {

    private BeanUtils() {
    }

    private static void handleReflectionException(Exception e) {
        ReflectionUtils.handleReflectionException(e);
    }


    /**
     * 属性拷贝
     *
     * @param orig      源对象
     * @param destClass 目标
     * @return T
     */
    public static <T> T copyProperties(Object orig, Class<T> destClass) {
        try {
            T target = destClass.newInstance();
            if (orig == null) {
                return null;
            }
            copyProperties(orig, target);
            return target;
        } catch (Exception e) {
            handleReflectionException(e);
            return null;
        }
    }


    public static void copyProperties(Object orig, Object dest) {
        try {
            org.springframework.beans.BeanUtils.copyProperties(orig, dest);
        } catch (Exception e) {
            handleReflectionException(e);
        }
    }

}