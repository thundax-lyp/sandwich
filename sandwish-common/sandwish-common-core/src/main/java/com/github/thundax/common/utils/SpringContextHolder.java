package com.github.thundax.common.utils;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Slf4j
public class SpringContextHolder {

    private static ApplicationContext applicationContext = null;

    public static void setApplicationContext(ApplicationContext context) {
        if (log.isDebugEnabled()) {
            log.debug("设置SpringContextHolder中的ApplicationContext:" + context);
        }
        applicationContext = context;
    }

    public static void clearHolder() {
        if (log.isDebugEnabled()) {
            log.debug("清除SpringContextHolder中的ApplicationContext:" + applicationContext);
        }
        applicationContext = null;
    }

    @NonNull
    public static <T> T getBean(Class<T> requiredType) {
        return applicationContext.getBean(requiredType);
    }

    /**
     * getBeansOfType
     *
     * @param requiredType requiredType
     * @param <T> T
     * @return beansOfType
     */
    @NonNull
    public static <T> Map<String, T> getBeansOfType(@Nullable Class<T> requiredType) {
        return applicationContext.getBeansOfType(requiredType);
    }
}
