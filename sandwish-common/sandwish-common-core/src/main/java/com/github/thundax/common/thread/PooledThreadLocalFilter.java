package com.github.thundax.common.thread;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/** @author thundax */
public class PooledThreadLocalFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Filter.default 在tomcat中并没有起作用，大概是和版本有关
    }

    @Override
    public void destroy() {
        // Filter.default 在tomcat中并没有起作用，大概是和版本有关
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } finally {
            PooledThreadLocal.reset();
        }
    }
}
