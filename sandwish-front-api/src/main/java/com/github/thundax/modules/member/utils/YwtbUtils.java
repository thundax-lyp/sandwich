package com.github.thundax.modules.member.utils;

import com.github.thundax.common.utils.SpringContextHolder;

/**
 * @Auther: zhangrudong
 * @Date: 2021/10/20 15:31
 * @Description:
 */
public class YwtbUtils {

    private static YwtbProperties properties;

    private static YwtbProperties getProperties() {
        if (properties == null) {
            properties = SpringContextHolder.getBean(YwtbProperties.class);
        }
        return properties;
    }

    public static String getLoginUrl(){
        return getProperties().getThirdLoginUrl();
    }

    public static String getLoginBakUrl(){
        return getProperties().getLoginBackUrl();
    }
}
