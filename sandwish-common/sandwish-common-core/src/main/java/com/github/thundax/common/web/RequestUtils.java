package com.github.thundax.common.web;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * HttpServletRequest帮助类
 *
 * @author thundax
 */
public class RequestUtils {

    private static final Logger log = LoggerFactory.getLogger(RequestUtils.class);

    public static HttpServletRequest currentRequest() {
        return ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
    }

    /**
     * 获取QueryString的参数，并使用URLDecoder以UTF-8格式转码。如果请求是以post方法提交的，
     * 那么将通过HttpServletRequest#getParameter获取。
     *
     * @param request web请求
     * @param name    参数名称
     */
    public static String getQueryParam(HttpServletRequest request, String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        if (isPost(request)) {
            return request.getParameter(name);
        }
        String s = request.getQueryString();
        if (StringUtils.isBlank(s)) {
            return null;
        }
        try {
            s = URLDecoder.decode(s, "utf-8");
        } catch (UnsupportedEncodingException e) {
            log.error("encoding utf-8 not support?", e);
        }
        String[] values = parseQueryString(s).get(name);
        if (values != null && values.length > 0) {
            return values[values.length - 1];
        } else {
            return null;
        }
    }

    public static Integer getQueryParamInteger(HttpServletRequest request, String name) {
        String value = getQueryParam(request, name);
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Boolean getQueryParamBoolean(HttpServletRequest request, String name) {
        String value = getQueryParam(request, name);
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return Boolean.parseBoolean(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Map<String, Object> getQueryParams(HttpServletRequest request) {
        Map<String, String[]> map;
        if (isPost(request)) {
            map = request.getParameterMap();
        } else {
            String s = request.getQueryString();
            if (StringUtils.isBlank(s)) {
                return new HashMap<>(16);
            }
            try {
                s = URLDecoder.decode(s, "utf-8");
            } catch (UnsupportedEncodingException e) {
                log.error("encoding utf-8 not support?", e);
            }
            map = parseQueryString(s);
        }

        Map<String, Object> params = new HashMap<>(map.size());
        int len;
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            len = entry.getValue().length;
            if (len == 1) {
                params.put(entry.getKey(), entry.getValue()[0]);
            } else if (len > 1) {
                params.put(entry.getKey(), entry.getValue());
            }
        }
        return params;
    }

    /**
     * Parses a query string passed from the client to the server and builds a
     * <code>HashTable</code> object with key-value pairs. The query string
     * should be in the form of a string packaged by the GET or POST method,
     * that is, it should have key-value pairs in the form <i>key=value</i>,
     * with each pair separated from the next by a &amp; character.
     * <p/>
     * <p/>
     * A key can appear more than once in the query string with different
     * values. However, the key appears only once in the hashtable, with its
     * value being an array of strings containing the multiple values sent by
     * the query string.
     * <p/>
     * <p/>
     * The keys and values in the hashtable are stored in their decoded form, so
     * any + characters are converted to spaces, and characters sent in
     * hexadecimal notation (like <i>%xx</i>) are converted to ASCII characters.
     *
     * @param s a string containing the query to be parsed
     * @return a <code>HashTable</code> object built from the parsed key-value
     * pairs
     * @throws IllegalArgumentException if the query string is invalid
     */
    public static Map<String, String[]> parseQueryString(String s) {
        String[] valArray;
        if (s == null) {
            throw new IllegalArgumentException();
        }
        Map<String, String[]> ht = new HashMap<>(16);
        StringTokenizer st = new StringTokenizer(s, "&");
        while (st.hasMoreTokens()) {
            String pair = st.nextToken();
            int pos = pair.indexOf('=');
            if (pos == -1) {
                continue;
            }
            String key = pair.substring(0, pos);
            String val = pair.substring(pos + 1, pair.length());
            if (ht.containsKey(key)) {
                String[] oldValues = ht.get(key);
                valArray = new String[oldValues.length + 1];
                for (int i = 0; i < oldValues.length; i++) {
                    valArray[i] = oldValues[i];
                }
                valArray[oldValues.length] = val;
            } else {
                valArray = new String[1];
                valArray[0] = val;
            }
            ht.put(key, valArray);
        }
        return ht;
    }

    public static Map<String, String> getRequestParamMap(HttpServletRequest request,
                                                         String prefix,
                                                         boolean nameWithPrefix) {
        Map<String, String> map = new HashMap<>(16);
        Enumeration<String> names = request.getParameterNames();
        String name, key, value;
        while (names.hasMoreElements()) {
            name = names.nextElement();
            if (name.startsWith(prefix)) {
                key = nameWithPrefix ? name : name.substring(prefix.length());
                value = StringUtils.join(request.getParameterValues(name), ',');
                if (StringUtils.isNotEmpty(value)) {
                    map.put(key, value);
                }
            }
        }
        return map;
    }

    /**
     * 获取访问者IP
     * <p/>
     * 在一般情况下使用Request.getRemoteAddr()即可，但是经过nginx等反向代理软件后，这个方法会失效。
     * <p/>
     * 本方法先从Header中获取X-Real-IP，如果不存在再从X-Forwarded-For获得第一个IP(用,分割)，
     * 如果还不存在则调用Request .getRemoteAddr()。
     */
    public static String getRemoteAddr(HttpServletRequest request) {
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

    public static String getRemoteAddr() {
        return getRemoteAddr(currentRequest());
    }


    public static boolean isGet(HttpServletRequest request) {
        return "get".equalsIgnoreCase(request.getMethod());
    }

    public static boolean isPost(HttpServletRequest request) {
        return "post".equalsIgnoreCase(request.getMethod());
    }

}
