package com.github.thundax.modules.storage.utils;

import com.github.thundax.common.utils.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author: someone
 * @date: 2023/7/25
 **/
public class HtmlResourceUtils {

    private static final String SERVLET_PREFIX = "/servlet/storage";

    /**
     * html 保存时调用
     * @param html
     * @return
     */
    public static String cleanResources(String html) {
        if (StringUtils.isEmpty(html)) {
            return html;
        }

        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();

        org.jsoup.nodes.Document doc = Jsoup.parse(html);
        for (org.jsoup.nodes.Element ele : doc.select("img, a, audio, video")) {
            String resourceUrl = StringUtils.EMPTY;
            if (StringUtils.equalsIgnoreCase(ele.tagName(), "img")) {
                resourceUrl = ele.attr("src");
            } else if (StringUtils.equalsIgnoreCase(ele.tagName(), "audio")) {
                resourceUrl = ele.attr("src");
            } else if (StringUtils.equalsIgnoreCase(ele.tagName(), "video")) {
                resourceUrl = ele.attr("src");
            } else if (StringUtils.equalsIgnoreCase(ele.tagName(), "a")) {
                resourceUrl = ele.attr("href");
            }

            if (!resourceUrl.startsWith("/")) {
                resourceUrl = "/" + resourceUrl;
            }
            if (!isContextPath(resourceUrl, request.getContextPath())) {
                continue;
            }
            html = StringUtils.replace(html, resourceUrl, StringUtils.replace(resourceUrl,request.getContextPath(),""));
        }

        return html;
    }


    /**
     * html 预览时调用
     * @param html
     * @return
     */
    public static String previewResources(String html) {

        if (StringUtils.isEmpty(html)) {
            return html;
        }

        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();

        org.jsoup.nodes.Document doc = Jsoup.parse(html);
        for (org.jsoup.nodes.Element ele : doc.select("img, a, audio, video")) {
            String resourceUrl = StringUtils.EMPTY;
            if (StringUtils.equalsIgnoreCase(ele.tagName(), "img")) {
                resourceUrl = ele.attr("src");
            } else if (StringUtils.equalsIgnoreCase(ele.tagName(), "audio")) {
                resourceUrl = ele.attr("src");
            } else if (StringUtils.equalsIgnoreCase(ele.tagName(), "video")) {
                resourceUrl = ele.attr("src");
            } else if (StringUtils.equalsIgnoreCase(ele.tagName(), "a")) {
                resourceUrl = ele.attr("href");
            }

            // 如果src没有指向本地资源，则不处理
            if (!isServletPath(resourceUrl)) {
                continue;
            }

            html = StringUtils.replace(html, resourceUrl, request.getContextPath() + resourceUrl);
        }

        return html;

    }

    private static boolean isContextPath(String path,String contextPath) {
        if (StringUtils.isAnyEmpty(path, contextPath)) {
            return false;
        }
        return path.startsWith(contextPath);
    }

    private static boolean isServletPath(String path) {
        if (StringUtils.isAnyEmpty(path)) {
            return false;
        }
        return path.startsWith(SERVLET_PREFIX);
    }
}
