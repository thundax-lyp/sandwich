package com.github.thundax.common.filter.xss;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;

/**
 * XssFilter
 *
 * @author wdit
 */
public class XssFilter implements Filter {

    private List<String> urlExcludes = new ArrayList<>();

    private List<String> excludeTags = new ArrayList<>();

    private List<String> includeTags = new ArrayList<>();

    public boolean enabled = false;

    private String encoding = "UTF-8";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String enabledStr = filterConfig.getInitParameter("enabled");
        String excludeUrlStr = filterConfig.getInitParameter("urlExcludes");
        String excludeTagStr = filterConfig.getInitParameter("tagExcludes");
        String includeTagStr = filterConfig.getInitParameter("tagIncludes");
        String encodingStr = filterConfig.getInitParameter("encoding");

        if (StringUtils.isNotEmpty(excludeUrlStr)) {
            String[] url = excludeUrlStr.split(",");
            Collections.addAll(this.urlExcludes, url);
        }

        if (StringUtils.isNotEmpty(excludeTagStr)) {
            String[] url = excludeTagStr.split(",");
            Collections.addAll(this.excludeTags, url);
        }

        if (StringUtils.isNotEmpty(includeTagStr)) {
            String[] url = includeTagStr.split(",");
            Collections.addAll(this.includeTags, url);
        }

        if (StringUtils.isNotEmpty(enabledStr)) {
            this.enabled = Boolean.parseBoolean(enabledStr);
        }

        if (StringUtils.isNotEmpty(encodingStr)) {
            this.encoding = encodingStr;
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        if (handleExcludeUrls(req, resp)) {
            chain.doFilter(request, response);
            return;
        }

        XssHttpServletRequestWrapper xssRequest =
                new XssHttpServletRequestWrapper((HttpServletRequest) request, encoding, excludeTags, includeTags);
        chain.doFilter(xssRequest, response);
    }

    @Override
    public void destroy() {}

    /**
     * 是否过滤
     *
     * @param request
     * @param response
     * @return
     */
    private boolean handleExcludeUrls(HttpServletRequest request, HttpServletResponse response) {
        if (!enabled) {
            return true;
        }
        String url = request.getServletPath();
        // 特殊路径
        if (urlExcludes == null || urlExcludes.isEmpty()) {
            return false;
        }
        for (String pattern : urlExcludes) {
            Pattern p = Pattern.compile("^" + pattern);
            Matcher m = p.matcher(url);
            if (m.find()) {
                return true;
            }
        }
        return false;
    }
}
