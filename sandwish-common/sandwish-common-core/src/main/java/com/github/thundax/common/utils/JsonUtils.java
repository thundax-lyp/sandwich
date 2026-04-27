package com.github.thundax.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;

/**
 * JSON工具类，使用jackson库
 *
 * @author thundax
 */
public class JsonUtils {

    private static ObjectMapper mapper = null;

    public static ObjectMapper getMapper() {
        if (mapper == null) {
            mapper = new ObjectMapper();
            // 忽略目标对象没有的属性
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
        return mapper;
    }

    public static <T> T fromJson(String jsonString, Class<T> type) {
        try {
            return getMapper().readValue(jsonString, type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T fromJson(String jsonString, TypeReference<T> valueTypeRef) {
        try {
            return getMapper().readValue(jsonString, valueTypeRef);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String toJson(Object bean) {
        try {
            return getMapper().writeValueAsString(bean);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return StringUtils.EMPTY;
        }
    }

    public static String toPrettyJson(Object bean) {
        try {
            return getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(bean);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return StringUtils.EMPTY;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T clone(T bean) {
        if (bean == null) {
            return null;
        }
        return (T) fromJson(toJson(bean), bean.getClass());
    }

    public static <T, R> R clone(T bean, Class<R> clazz) {
        if (bean == null) {
            return null;
        }
        return fromJson(toJson(bean), clazz);
    }

    public static <T, R> R clone(T bean, TypeReference<R> typeReference) {
        if (bean == null) {
            return null;
        }
        return fromJson(toJson(bean), typeReference);
    }
}
