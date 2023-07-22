package com.bx.implatform.filter;

import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@Component
@ServletComponentScan
@WebFilter(urlPatterns = "/*",filterName = "xssFilter")
public class CacheFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig)  {}

    @Override
    public void destroy() {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        chain.doFilter(new CacheHttpServletRequestWrapper((HttpServletRequest) request), response);
    }

}