package com.bx.implatform.service.impl;


import com.alibaba.fastjson.JSON;
import com.bx.common.util.BeanUtils;
import com.bx.implatform.entity.User;
import com.bx.implatform.service.IUserService;
import com.bx.implatform.session.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SecurityUserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private IUserService userService;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findUserByName(username);
        if(user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        //定义权限列表.
        List<GrantedAuthority> authorities = new ArrayList();
        // 用户可以访问的资源名称（或者说用户所拥有的权限） 注意：必须"ROLE_"开头
        authorities.add(new SimpleGrantedAuthority("ROLE_XX"));

        UserSession session = BeanUtils.copyProperties(user,UserSession.class);
        String strJson = JSON.toJSONString(session);
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(strJson,user.getPassword(),authorities);
        return userDetails;
    }
}
