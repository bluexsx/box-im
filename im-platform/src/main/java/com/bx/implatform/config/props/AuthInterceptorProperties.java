package com.bx.implatform.config.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "auth-interceptor")
public class AuthInterceptorProperties {

    /**
     * 登录拦截器放行的 URL 路径
     */
    private List<String> excludePaths = new ArrayList<>();

}
