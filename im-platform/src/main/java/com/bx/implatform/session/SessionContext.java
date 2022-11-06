package com.bx.implatform.session;

import com.alibaba.fastjson.JSON;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

/*
 * @Description
 * @Author Blue
 * @Date 2022/10/21
 */
public class SessionContext {


    public static UserSession getSession(){
        User useDetail = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String strJson = useDetail.getUsername();
        UserSession userSession = JSON.parseObject(strJson,UserSession.class);
        return  userSession;
    }


}
