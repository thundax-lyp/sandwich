package com.github.thundax.common.utils;

import com.github.thundax.common.utils.encrypt.SM4Util;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * 本地化配置文件取值转换器，用于处理配置文件中加密属性
 *
 * <p>加密方式使用Sm4.encrypt(data, EncryptUtil.KEY)
 *
 * @author wdit
 * @date 2019/3/31
 */
public class NativePropertyPlaceConfigurer extends PropertyPlaceholderConfigurer {

    /* 加密盐值() */
    public static final String SALT = "PJ-1712PJ-1712";
    public static final String PREFIX = "ENC(WDIT:";

    @Override
    protected String convertProperty(String propertyName, String propertyValue) {

        // 加密属性处理
        if (propertyValue.startsWith(PREFIX)) {
            try {
                return SM4Util.decryptEcb(SALT, propertyValue.substring(PREFIX.length(), propertyValue.length() - 1));
            } catch (Exception e) {
                e.printStackTrace();
                return propertyValue;
            }
        }

        return propertyValue;
    }

    public static void main(String[] args) {
        System.out.println("sa加密结果为：ENC(WDIT:" + SM4Util.encryptEcb(SALT, "sa") + ")");
        System.out.println("wdit@123East加密结果为：ENC(WDIT:" + SM4Util.encryptEcb(SALT, "wdit@123East") + ")");
    }
}
