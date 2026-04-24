package com.github.thundax.common.filter.xss;

import com.github.thundax.common.web.RequestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * XSS过滤处理
 * @author wdit
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private HttpServletRequest orgRequest;

    private String encoding;

    private HTMLFilter htmlFilter;

    private final static String JSON_CONTENT_TYPE = "application/json";

    private final static String CONTENT_TYPE = "Content-Type";


    /**
     * @param request HttpServletRequest
     * @param encoding 编码
     * @param excludeTags 例外的特定标签
     * @param includeTags 需要过滤的标签
     */
    public XssHttpServletRequestWrapper(HttpServletRequest request, String encoding, List<String> excludeTags, List<String> includeTags ){
        super( request );
        orgRequest = request;
        this.encoding = encoding;
        this.htmlFilter = new HTMLFilter();
    }

    /**
     *
     * @param request HttpServletRequest
     * @param encoding 编码
     */
    public XssHttpServletRequestWrapper(HttpServletRequest request, String encoding ){
        this( request, encoding, null, null );
    }

    private String xssFilter( String input ){
        return htmlFilter.filter( input );
    }

    @Override
    public ServletInputStream getInputStream() throws IOException{
        // 非json处理
        if( !JSON_CONTENT_TYPE.equalsIgnoreCase( super.getHeader( CONTENT_TYPE ) ) ){
            return super.getInputStream();
        }
        InputStream in = super.getInputStream();
        String body = IOUtils.toString( in, encoding );
        IOUtils.closeQuietly( in );

        //空串处理直接返回
        if( StringUtils.isBlank( body ) ){
            return super.getInputStream();
        }

        // xss过滤
        body = XssShieldUtil.stripXss(body);
        body = xssFilter(URLDecoder.decode(body, encoding));
        return new RequestCachingInputStream( body.getBytes( encoding ) );

    }

    @Override
    public String getParameter( String name ){
        try {
            String value = super.getParameter(xssFilter(URLDecoder.decode(name, encoding)));
            if (StringUtils.isNotBlank(value)) {
                value = XssShieldUtil.stripXss(value);
                value = xssFilter(URLDecoder.decode(value, encoding));
            }
            return value;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String[] getParameterValues( String name ){
        String[] parameters = super.getParameterValues( name );
        if( parameters == null || parameters.length == 0 ){
            return null;
        }
        for( int i = 0; i < parameters.length; i++ ){
            try {
                parameters[i] = XssShieldUtil.stripXss(parameters[i]);
                parameters[i] = xssFilter(URLDecoder.decode(parameters[i], encoding));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return parameters;
    }

    @Override
    public Map<String, String[]> getParameterMap(){
        Map<String, String[]> map = new LinkedHashMap<>();
        Map<String, String[]> parameters = super.getParameterMap();
        for( String key : parameters.keySet() ){
            String[] values = parameters.get( key );
            for( int i = 0; i < values.length; i++ ){
                try {
                    values[i] = XssShieldUtil.stripXss(values[i]);
                    values[i] = xssFilter(URLDecoder.decode(values[i], encoding));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            map.put( key, values );
        }
        return map;
    }

    @Override
    public String getHeader( String name ){
        try {
            String value = super.getHeader(URLDecoder.decode(xssFilter(name), encoding));
            if (StringUtils.isNotBlank(value)) {
                value = XssShieldUtil.stripXss(value);
                value = xssFilter(URLDecoder.decode(value, encoding));
            }
            return value;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getQueryString() {
        String paramStr = "";
        String params = super.getQueryString();
        if (StringUtils.isBlank(params)) {
            return paramStr;
        }
        Map<String, String[]> map = new HashMap<>();
        try {
            map = RequestUtils.parseQueryString(params);
            for (Map.Entry<String, String[]> entry : map.entrySet()) {
                int len = entry.getValue().length;
                if (len == 1) {
                    String values = XssShieldUtil.stripXss(entry.getValue()[0]);
                    values = xssFilter(URLDecoder.decode(values, encoding));
                    if (StringUtils.isBlank(paramStr)) {
                        paramStr += entry.getKey() + "=" + values;
                    } else {
                        paramStr += "&" + entry.getKey() + "=" + values;
                    }
                } else if (len > 1) {
                    for (String values : entry.getValue()) {
                        values = XssShieldUtil.stripXss(values);
                        values = xssFilter(URLDecoder.decode(values, encoding));
                        if (StringUtils.isBlank(paramStr)) {
                            paramStr += entry.getKey() + "=" + values;
                        } else {
                            paramStr += "&" + entry.getKey() + "=" + values;
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return paramStr;
    }

    /**
     * <b>
     * #获取最原始的request
     * </b>
     */
    public HttpServletRequest getOrgRequest(){
        return orgRequest;
    }

    /**
     * <b>
     * #获取最原始的request
     * </b>
     * @param request HttpServletRequest
     */
    public static HttpServletRequest getOrgRequest(HttpServletRequest request ){
        if( request instanceof XssHttpServletRequestWrapper){
            return ((XssHttpServletRequestWrapper) request).getOrgRequest();
        }
        return request;
    }

    /**
     * <pre>
     * servlet中inputStream只能一次读取，后续不能再次读取inputStream
     * xss过滤body后，重新把流放入ServletInputStream中
     * </pre>
     */
    private static class RequestCachingInputStream extends ServletInputStream {
        private final ByteArrayInputStream inputStream;
        public RequestCachingInputStream(byte[] bytes) {
            inputStream = new ByteArrayInputStream(bytes);
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }

        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener( ReadListener readListener ){
        }
    }
}
