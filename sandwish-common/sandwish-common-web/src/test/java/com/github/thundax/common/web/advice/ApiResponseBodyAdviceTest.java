package com.github.thundax.common.web.advice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.github.thundax.common.web.annotation.WrappedApiController;
import com.github.thundax.common.web.annotation.WrappedApiResponse;
import com.github.thundax.common.web.response.ApiResponse;
import com.github.thundax.common.web.response.PageResponse;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestController;

public class ApiResponseBodyAdviceTest {

    private final ApiResponseBodyAdvice advice = new ApiResponseBodyAdvice();

    @Test
    public void shouldSupportWrappedControllerPlainResponse() throws Exception {
        assertTrue(advice.supports(
                returnType(WrappedController.class, "plain"), MappingJackson2HttpMessageConverter.class));
    }

    @Test
    public void shouldSupportUnwrappedControllerJsonContentType() throws Exception {
        assertTrue(advice.supports(
                returnType(UnwrappedController.class, "plain"), MappingJackson2HttpMessageConverter.class));
    }

    @Test
    public void shouldSupportMethodWrappedResponse() throws Exception {
        assertTrue(advice.supports(
                returnType(MethodWrappedController.class, "plain"), MappingJackson2HttpMessageConverter.class));
    }

    @Test
    public void shouldSupportAlreadyWrappedResponseJsonContentType() throws Exception {
        assertTrue(advice.supports(
                returnType(WrappedController.class, "apiResponse"), MappingJackson2HttpMessageConverter.class));
    }

    @Test
    public void shouldSupportStringResponseJsonContentType() throws Exception {
        assertTrue(advice.supports(
                returnType(WrappedController.class, "text"), MappingJackson2HttpMessageConverter.class));
    }

    @Test
    public void shouldSupportPageResponseJsonContentType() throws Exception {
        assertTrue(advice.supports(
                returnType(WrappedController.class, "page"), MappingJackson2HttpMessageConverter.class));
    }

    @Test
    public void shouldWrapPlainBody() throws Exception {
        Object result = advice.beforeBodyWrite(
                100,
                returnType(WrappedController.class, "plain"),
                MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                null,
                null);

        ApiResponse<?> response = (ApiResponse<?>) result;
        assertEquals(ApiResponse.SUCCESS_CODE, response.getCode());
        assertEquals(100, response.getData());
    }

    @Test
    public void shouldSetJsonUtf8ContentType() throws Exception {
        TestServerHttpResponse response = new TestServerHttpResponse();

        advice.beforeBodyWrite(
                100,
                returnType(WrappedController.class, "plain"),
                MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                null,
                response);

        assertEquals(
                "application/json;charset=UTF-8",
                response.getHeaders().getContentType().toString());
    }

    @Test
    public void shouldKeepAlreadyWrappedBody() throws Exception {
        ApiResponse<Object> body = ApiResponse.success();

        Object result = advice.beforeBodyWrite(
                body,
                returnType(WrappedController.class, "apiResponse"),
                MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                null,
                null);

        assertSame(body, result);
    }

    @Test
    public void shouldWrapPageBody() throws Exception {
        PageResponse<Object> body = new PageResponse<>();

        Object result = advice.beforeBodyWrite(
                body,
                returnType(WrappedController.class, "page"),
                MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                null,
                null);

        ApiResponse<?> response = (ApiResponse<?>) result;
        assertEquals(ApiResponse.SUCCESS_CODE, response.getCode());
        assertSame(body, response.getData());
    }

    @Test
    public void shouldKeepUnwrappedControllerBody() throws Exception {
        Object body = 100;

        Object result = advice.beforeBodyWrite(
                body,
                returnType(UnwrappedController.class, "plain"),
                MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                null,
                null);

        assertSame(body, result);
    }

    private MethodParameter returnType(Class<?> controllerClass, String methodName) throws Exception {
        Method method = controllerClass.getDeclaredMethod(methodName);
        return new MethodParameter(method, -1);
    }

    private static class TestServerHttpResponse implements ServerHttpResponse {

        private final HttpHeaders headers = new HttpHeaders();
        private final ByteArrayOutputStream body = new ByteArrayOutputStream();

        @Override
        public void setStatusCode(HttpStatus status) {}

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

        @Override
        public OutputStream getBody() {
            return body;
        }

        @Override
        public void flush() {}

        @Override
        public void close() {}
    }

    @WrappedApiController
    private static class WrappedController {

        public Object plain() {
            return null;
        }

        public ApiResponse<Object> apiResponse() {
            return null;
        }

        public String text() {
            return null;
        }

        public PageResponse<Object> page() {
            return null;
        }
    }

    @RestController
    private static class UnwrappedController {

        public Object plain() {
            return null;
        }
    }

    @RestController
    private static class MethodWrappedController {

        @WrappedApiResponse
        public Object plain() {
            return null;
        }
    }
}
