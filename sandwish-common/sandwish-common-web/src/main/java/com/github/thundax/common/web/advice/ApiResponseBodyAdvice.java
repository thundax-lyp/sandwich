package com.github.thundax.common.web.advice;

import com.github.thundax.common.web.annotation.WrappedApiController;
import com.github.thundax.common.web.annotation.WrappedApiResponse;
import com.github.thundax.common.web.response.ApiResponse;
import com.github.thundax.common.web.response.PageResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ApiResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> containingClass = returnType.getContainingClass();
        boolean wrappedController =
                containingClass != null && containingClass.isAnnotationPresent(WrappedApiController.class);
        if (!wrappedController && !returnType.hasMethodAnnotation(WrappedApiResponse.class)) {
            return false;
        }

        Class<?> parameterType = returnType.getParameterType();
        return !ApiResponse.class.isAssignableFrom(parameterType)
                && !String.class.isAssignableFrom(parameterType)
                && !PageResponse.class.isAssignableFrom(parameterType);
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {
        if (body instanceof ApiResponse || body instanceof String || body instanceof PageResponse) {
            return body;
        }
        return ApiResponse.success(body);
    }
}
