package com.github.thundax.common.thread;

import javax.servlet.*;
import java.io.IOException;

/**
 * @author thundax
 */
public class PooledThreadLocalFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //Filter.default 在tomcat中并没有起作用，大概是和版本有关
    }

    @Override
    public void destroy() {
        //Filter.default 在tomcat中并没有起作用，大概是和版本有关
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } finally {
            PooledThreadLocal.reset();
        }
    }

}
