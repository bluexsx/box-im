package com.bx.implatform.config;

import com.alibaba.fastjson.JSON;
import com.bx.implatform.enums.ResultCode;
import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.service.IUserService;
import com.bx.implatform.session.UserSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.PrintWriter;

/*
 * SpringSecurity安全框架配置
 *
 * @Author Blue
 * @Date 2022/10/21
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled=true)
public class WebSecurityConfg extends WebSecurityConfigurerAdapter {



    @Qualifier("securityUserDetailsServiceImpl")
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private IUserService userService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/image/upload","/login","/logout","/register","/swagger-resources/**", "/webjars/**", "/v2/**", "/swagger-ui.html/**")
            .permitAll()
            .anyRequest() //任何其它请求
            .authenticated() //都需要身份认证
            .and()
            // 登录配置表单认证方式
            .formLogin()
            .usernameParameter("username")//设置登录账号参数，与表单参数一致
            .passwordParameter("password")//设置登录密码参数，与表单参数一致
            .loginProcessingUrl("/login")//配置默认登录入口
            .successHandler(successHandler())
            .failureHandler(failureHandler())
            .and()
            // 注销
            .logout()
            .logoutUrl("/logout")
            .logoutSuccessHandler(logoutHandler())
            .permitAll()
            .and()
            // session管理
            .sessionManagement()
            .and()
            // 禁用跨站csrf攻击防御
            .csrf()
            .disable()
                .exceptionHandling()
                .authenticationEntryPoint(entryPoint());

    }

    @Bean
    AuthenticationFailureHandler failureHandler(){
        return (request, response, exception) -> {
            response.setContentType("application/json;charset=utf-8");
            PrintWriter out = response.getWriter();
            Result result = ResultUtils.error(ResultCode.LOGIN_ERROR,exception.getMessage());
            if (exception instanceof LockedException) {
                result =ResultUtils.error(ResultCode.LOGIN_ERROR,"账户被锁定，请联系管理员!");
            } else if (exception instanceof CredentialsExpiredException) {
                result = ResultUtils.error(ResultCode.LOGIN_ERROR,"密码过期，请联系管理员!");
            } else if (exception instanceof AccountExpiredException) {
                result =ResultUtils.error(ResultCode.LOGIN_ERROR,"账户过期，请联系管理员!");
            } else if (exception instanceof DisabledException) {
                result = ResultUtils.error(ResultCode.LOGIN_ERROR,"账户被禁用，请联系管理员!");
            } else if (exception instanceof BadCredentialsException) {
                result =ResultUtils.error(ResultCode.LOGIN_ERROR,"用户名或者密码输入错误，请重新输入!");
            }
            out.write(new ObjectMapper().writeValueAsString(result));
            out.flush();
            out.close();
        };
    }

    @Bean
    AuthenticationSuccessHandler successHandler(){
        return (request, response, authentication) -> {
            User useDetail = (User)authentication.getPrincipal();
            String strJson = useDetail.getUsername();
            UserSession userSession = JSON.parseObject(strJson,UserSession.class);
            log.info("用户 '{}' 登录,id:{},昵称:{}",userSession.getUserName(),userSession.getId(),userSession.getNickName());
            // 响应
            response.setContentType("application/json;charset=utf-8");
            PrintWriter out = response.getWriter();
            Result result = ResultUtils.success();
            out.write(new ObjectMapper().writeValueAsString(result));
            out.flush();
            out.close();

        };
    }


    @Bean
    LogoutSuccessHandler logoutHandler(){
        return (request, response, authentication) -> {
            User useDetail = (User)authentication.getPrincipal();
            String strJson = useDetail.getUsername();
            UserSession userSession = JSON.parseObject(strJson,UserSession.class);
            log.info("用户 '{}' 退出,id:{},昵称:{}",userSession.getUserName(),userSession.getId(),userSession.getNickName());
            // 响应
            response.setContentType("application/json;charset=utf-8");
            PrintWriter out = response.getWriter();
            Result result = ResultUtils.success();
            out.write(new ObjectMapper().writeValueAsString(result));
            out.flush();
            out.close();
        };
    }

    @Bean
    AuthenticationEntryPoint entryPoint(){
        return (request, response, exception) -> {
            response.setContentType("application/json;charset=utf-8");
            PrintWriter out = response.getWriter();
            Result result = ResultUtils.error(ResultCode.NO_LOGIN);
            out.write(new ObjectMapper().writeValueAsString(result));
            out.flush();
            out.close();
        };
    }



    @Bean
    public PasswordEncoder passwordEncoder(){
        // 使用BCrypt加密密码
        return new BCryptPasswordEncoder();
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }


}
