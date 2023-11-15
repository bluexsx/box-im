package com.bx.implatform.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

//@Configuration
public class GlobalCorsConfig {

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        FilterRegistrationBean<CorsFilter> corsFilterFilterRegistrationBean = new FilterRegistrationBean<>();
        //添加CORS配置信息
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //允许的域，不要写*，否则cookie就无法使用了
        corsConfiguration.addAllowedOrigin("*");
        //允许的头信息
        corsConfiguration.addAllowedHeader("*");
        //允许的请求方式
        corsConfiguration.setAllowedMethods(Arrays.asList("POST", "PUT", "GET", "OPTIONS", "DELETE"));
        //是否发送cookie信息
        corsConfiguration.setAllowCredentials(true);
        //预检请求的有效期，单位为秒
        corsConfiguration.setMaxAge(3600L);

        //添加映射路径，标识待拦截的请求
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        corsFilterFilterRegistrationBean.setFilter(new CorsFilter(source));
        corsFilterFilterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return corsFilterFilterRegistrationBean;
    }
}
