package com.github.thundax.common.web;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 执行时间过滤器
 *
 * @author thundax
 */
public class ProcessTimeFilter implements Filter {

    protected static final Logger log = LoggerFactory.getLogger(ProcessTimeFilter.class);

    public static final String START_TIME = "_start_time";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Filter.default 在tomcat中并没有起作用，大概是和版本有关
    }

    @Override
    public void destroy() {
        // Filter.default 在tomcat中并没有起作用，大概是和版本有关
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;

        String remoteAddr = getRemoteAddr(request);

        long time = System.currentTimeMillis();
        request.setAttribute(START_TIME, time);

        System.out.println("=== " + request.getRequestURI());

        chain.doFilter(request, response);

        time = System.currentTimeMillis() - time;
        log.debug("process {} ms[{}] from[{}]", request.getRequestURI(), time, remoteAddr);
    }

    private String getRemoteAddr(HttpServletRequest request) {
        String remoteAddr = request.getHeader("X-Real-IP");
        if (StringUtils.isNotBlank(remoteAddr)) {
            return remoteAddr;
        }

        remoteAddr = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(remoteAddr)) {
            return remoteAddr;
        }

        remoteAddr = request.getHeader("Proxy-Client-IP");
        if (StringUtils.isNotBlank(remoteAddr)) {
            return remoteAddr;
        }

        remoteAddr = request.getHeader("WL-Proxy-Client-IP");
        if (StringUtils.isNotBlank(remoteAddr)) {
            return remoteAddr;
        }

        return request.getRemoteAddr();
    }
}
