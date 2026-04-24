package com.github.thundax.common.utils;

import com.github.thundax.common.utils.encrypt.SM4Util;

public class EncryptUtils {

    /**
     * sm4加密
     * @param value
     * @return
     */
    public static String sm4Encrypt(String value) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        try {
            String encryptedValue = SM4Util.encryptEcb(NativePropertyPlaceConfigurer.SALT, value);
            return encryptedValue == null ? value : NativePropertyPlaceConfigurer.PREFIX + encryptedValue;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * sm4解密
     * @param value
     * @return
     */
    public static String sm4Decrypt(String value) {
        if (!StringUtils.startsWith(value, NativePropertyPlaceConfigurer.PREFIX)) {
            return value;
        }
        try {
            String decryptValue = SM4Util.decryptEcb(NativePropertyPlaceConfigurer.SALT, StringUtils.substring(value, NativePropertyPlaceConfigurer.PREFIX.length()));
            return decryptValue == null ? value : decryptValue;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

}
