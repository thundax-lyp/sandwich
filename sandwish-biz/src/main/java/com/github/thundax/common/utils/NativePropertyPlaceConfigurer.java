package com.github.thundax.common.utils;

import com.github.thundax.common.crypto.Sm4Crypto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

@Slf4j
public class NativePropertyPlaceConfigurer extends PropertyPlaceholderConfigurer {

    public static final String SALT = "PJ-1712PJ-1712";
    public static final String PREFIX = "ENC(WDIT:";

    @Override
    protected String convertProperty(String propertyName, String propertyValue) {

        if (propertyValue.startsWith(PREFIX)) {
            try {
                return Sm4Crypto.decryptEcb(SALT, propertyValue.substring(PREFIX.length(), propertyValue.length() - 1));
            } catch (Exception e) {
                log.warn("can not decrypt local property {}", propertyName, e);
                return propertyValue;
            }
        }

        return propertyValue;
    }

    public static void main(String[] args) {
        System.out.println("sa加密结果为：ENC(WDIT:" + Sm4Crypto.encryptEcb(SALT, "sa") + ")");
        System.out.println("wdit@123East加密结果为：ENC(WDIT:" + Sm4Crypto.encryptEcb(SALT, "wdit@123East") + ")");
    }
}
