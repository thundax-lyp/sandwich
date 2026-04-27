package com.github.thundax.modules.utils;

import com.github.thundax.autoconfigure.WebProperties;
import com.github.thundax.common.utils.SpringContextHolder;

/** @Auther: zhangrudong @Date: 2021/10/20 15:31 @Description: */
public class WebUtils {

    private static WebProperties properties;

    private static WebProperties getProperties() {
        if (properties == null) {
            properties = SpringContextHolder.getBean(WebProperties.class);
        }
        return properties;
    }

    public static String getBaseUrl() {
        return getProperties().getBaseUrl();
    }
}
