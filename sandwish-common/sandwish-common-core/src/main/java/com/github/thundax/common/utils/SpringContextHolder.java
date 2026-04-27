package com.github.thundax.common.utils;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/** @author thundax */
public class SpringContextHolder {

    private static final Logger logger = LoggerFactory.getLogger(SpringContextHolder.class);

    private static ApplicationContext applicationContext = null;

    public static void setApplicationContext(ApplicationContext context) {
        if (logger.isDebugEnabled()) {
            logger.debug("设置SpringContextHolder中的ApplicationContext:" + context);
        }
        applicationContext = context;
    }

    public static void clearHolder() {
        if (logger.isDebugEnabled()) {
            logger.debug("清除SpringContextHolder中的ApplicationContext:" + applicationContext);
        }
        applicationContext = null;
    }

    /** 根据Class类型获取bean */
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
