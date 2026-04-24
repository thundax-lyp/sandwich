package com.github.thundax.modules.auth.filter;

import com.github.thundax.autoconfigure.VltavaProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 包装API返回值
 *
 * @author wdit
 */
public class ResponseWrapperFilter extends OncePerRequestFilter {

    /**
     * {"code":
     */
    private static final byte[] WRAPPED_RESPONSE_PREFIX = {
            '{', '\"', 'c', 'o', 'd', 'e', '\"', ':'
    };

    /**
     * {"code":0,"message":"success","data":
     */
    private static final byte[] RESPONSE_BODY_PREFIX = {
            '{',
            '\"', 'c', 'o', 'd', 'e', '\"', ':', '0', ',',
            '\"', 'm', 'e', 's', 's', 'a', 'g', 'e', '\"', ':', '\"', 's', 'u', 'c', 'c', 'e', 's', 's', '\"', ',',
            '\"', 'd', 'a', 't', 'a', '\"', ':'
    };

    private static final byte[] RESPONSE_BODY_SUFFIX = {
            '}'
    };

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final List<String> excludePatternList = new ArrayList<>();

    public ResponseWrapperFilter(VltavaProperties.ResponseWrapperFilterProperties properties) {
        this.excludePatternList.addAll(properties.getExcludePath());
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String requestUri = request.getRequestURI().substring(request.getContextPath().length());

        for (String excludePattern : excludePatternList) {
            if (pathMatcher.match(excludePattern, requestUri)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        ResponseWrapper responseWrapper = new ResponseWrapper(response);

        filterChain.doFilter(request, responseWrapper);

        ServletOutputStream outputStream = response.getOutputStream();
        if (!isJsonResponse(response)) {
            outputStream.write(responseWrapper.toBytes());
            return;
        }

        int statusCode = response.getStatus();
        if (statusCode != HttpStatus.OK.value()) {
            response.setStatus(HttpStatus.OK.value());
            outputStream.write(responseWrapper.toBytes());
            return;
        }

        byte[] responseBody = responseWrapper.toBytes();
        if (isWrappedResponse(responseBody)) {
            outputStream.write(responseBody);

        } else {
            response.setContentLength(responseBody.length + RESPONSE_BODY_PREFIX.length + RESPONSE_BODY_SUFFIX.length);
            outputStream.write(RESPONSE_BODY_PREFIX);
            outputStream.write(responseBody);
            outputStream.write(RESPONSE_BODY_SUFFIX);
        }
    }

    private boolean isJsonResponse(HttpServletResponse response) {
        String contentType = response.getContentType();
        return MediaType.APPLICATION_JSON_VALUE.equals(contentType)
                || MediaType.APPLICATION_JSON_UTF8_VALUE.equals(contentType);
    }

    private boolean isWrappedResponse(byte[] body) {
        if (body == null || body.length < WRAPPED_RESPONSE_PREFIX.length) {
            return false;
        }

        for (int idx = 0; idx < WRAPPED_RESPONSE_PREFIX.length; idx++) {
            if (body[idx] != WRAPPED_RESPONSE_PREFIX[idx]) {
                return false;
            }
        }

        return true;
    }

    public static class ResponseWrapper extends HttpServletResponseWrapper {
        private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        private ServletOutputStream servletOutputStream;

        public ResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public ServletOutputStream getOutputStream() {
            if (servletOutputStream == null) {
                servletOutputStream = new ServletOutputStream() {
                    @Override
                    public void write(int b) {
                        byteArrayOutputStream.write(b);
                    }

                    @Override
                    public boolean isReady() {
                        return false;
                    }

                    @Override
                    public void setWriteListener(WriteListener listener) {
                    }
                };
            }
            return servletOutputStream;
        }

        public byte[] toBytes() {
            return byteArrayOutputStream.toByteArray();
        }
    }

}
