package com.github.thundax.common.builder;

import com.github.thundax.common.persistence.Page;
import com.github.thundax.common.utils.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @param <T>
 * @author thundax
 */
public class PageBuilder<T> {

    private static final String RELOAD = "reload";

    private String cookiePrefix = StringUtils.EMPTY;

    private HttpServletRequest request;
    private HttpServletResponse response;

    public PageBuilder<T> cookiePrefix(String cookiePrefix) {
        this.cookiePrefix = cookiePrefix;
        return this;
    }

    public PageBuilder<T> request(HttpServletRequest request) {
        this.request = request;
        return this;
    }

    public PageBuilder<T> response(HttpServletResponse response) {
        this.response = response;
        return this;
    }

    public PageBuilder<T> servlet(HttpServletRequest request, HttpServletResponse response) {
        return this.request(request).response(response);
    }

    public Page<T> build() {
        Assert.notNull(request, "request can not be null");
        Assert.notNull(response, "response can not be null");

        int pageNo = Page.FIRST_PAGE_INDEX, pageSize = Page.DEFAULT_PAGE_SIZE;

        String pageNoString = request.getParameter("pageNo");
        if (StringUtils.isNumeric(pageNoString)) {
            CookieUtils.setCookie(response, cookiePrefix + "pageNo", pageNoString);
            pageNo = Integer.parseInt(pageNoString);

        } else if (request.getParameter(RELOAD) != null) {
            pageNoString = CookieUtils.getCookie(request, cookiePrefix + "pageNo");
            if (StringUtils.isNumeric(pageNoString)) {
                pageNo = Integer.parseInt(pageNoString);
            }

        } else {
            CookieUtils.setCookie(response, cookiePrefix + "pageNo", String.valueOf(pageNo));
        }

        String pageSizeString = request.getParameter("pageSize");
        if (StringUtils.isNumeric(pageSizeString)) {
            CookieUtils.setCookie(response, cookiePrefix + "pageSize", pageSizeString);
            pageSize = Integer.parseInt(pageSizeString);

        } else if (request.getParameter(RELOAD) != null) {
            pageSizeString = CookieUtils.getCookie(request, cookiePrefix + "pageSize");
            if (StringUtils.isNumeric(pageSizeString)) {
                pageSize = Integer.parseInt(pageSizeString);
            }

        } else {
            CookieUtils.setCookie(response, cookiePrefix + "pageSize", String.valueOf(pageSize));
        }

        Page<T> page = new Page<>();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        return page;
    }

}
