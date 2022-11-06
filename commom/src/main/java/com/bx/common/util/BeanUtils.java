package com.bx.common.util;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.ReflectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BeanUtils {
	

    
	private static void handleReflectionException(Exception e) {
		ReflectionUtils.handleReflectionException(e);
	}
	


	public static <T, U> List<U> copyProperties(List<T> sourceList, Class<U> clazz) {
		
		if(sourceList == null || sourceList.size() <= 0) {
			return new ArrayList<>();
		}
		
        List<U> result = new ArrayList<>();
        for (T source : sourceList) {
        	result.add(copyProperties(source, clazz));
    	}
        
        return result;
    }
	/**
	 * 如果source , 为空返回空对象
	 * @param sourceList
	 * @param clazz
	 * @return
	 */
    public static <T, U> List<U> copyPropertiesList(List<T> sourceList, Class<U> clazz) {
		
		if(sourceList == null || sourceList.size() <= 0) {
			return new ArrayList<U>();
		}
		
        List<U> result = new ArrayList<>();
        U target = null;
        for (T source : sourceList) {
    		try {
                target = clazz.newInstance();
                copyProperties(source, target);
    		}catch(Exception e) {
            	handleReflectionException(e);
    			return new ArrayList<>();
    		}
            result.add(target);
    	}
        
        return result;
    }

	/**
	 * source空为null
	 * @param orig
	 * @param destClass
	 * @param <T>
	 * @return
	 */
	public static <T> T copyProperties(Object orig, Class<T> destClass) {

		try {
			Object target = destClass.newInstance();
			if(orig == null) {
				return null;
			}
			copyProperties(orig, (Object)target);
			return (T) target;
		}catch(Exception e) {
			handleReflectionException(e);
			return null;
		}
	}

	/**
	 * source 为null 返回 空对象
	 * @param orig
	 * @param destClass
	 * @param <T>
	 * @return
	 */
	public static <T> T copyProperty(Object orig, Class<T> destClass) {

		try {
			Object target = destClass.newInstance();
			if(orig == null) {
				return (T)target;
			}

			copyProperties(orig, (Object)target);
			return (T) target;
		}catch(Exception e) {
			handleReflectionException(e);
			Object o1 = new Object();
			try {
				o1	= destClass.newInstance();
			}catch(Exception ex) {
				handleReflectionException(e);
			}
			return (T)o1;
		}
	}
	
	public static <T, U> List<U> copyProperties(List<T> sourceList, Class<U> clazz, String... ignoreProperties) {
		
		if(sourceList == null || sourceList.size() <= 0) {
			return new ArrayList<U>();
		}
		
        List<U> result = new ArrayList<>();
        for (T source : sourceList) {
        	result.add(copyProperties(source, clazz, ignoreProperties));
    	}
        
        return result;
    }

	public static <T> T copyProperties(Object orig, Class<T> destClass, String... ignoreProperties) {
		
		if(orig == null) {
			return null;
		}
		
		try {
			Object target = destClass.newInstance();
			org.springframework.beans.BeanUtils.copyProperties(orig, (Object)target, ignoreProperties);
			return (T)target;
		}catch(Exception e) {
			return null;
		}
	}
	
	public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper beanWrapper = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] propDesc = beanWrapper.getPropertyDescriptors();

        Set<String> emptynames = new HashSet<String>();
        
        for(java.beans.PropertyDescriptor pd : propDesc) {
            Object srcValue = beanWrapper.getPropertyValue(pd.getName());
            if (srcValue == null)  {emptynames.add(pd.getName());}
        }
        
        String[] result = new String[emptynames.size()];
        return emptynames.toArray(result);
    }
	
	public static void copyProperties(Object orig, Object dest) {
		try {
			org.springframework.beans.BeanUtils.copyProperties(orig, dest);
		} catch (Exception e) {
			handleReflectionException(e);
		}
	}

}