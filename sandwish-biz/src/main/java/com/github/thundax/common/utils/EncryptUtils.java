package com.github.thundax.common.utils;

import com.github.thundax.common.crypto.Sm4Crypto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class EncryptUtils {

    public static String sm4Encrypt(String value) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        try {
            String encryptedValue = Sm4Crypto.encryptEcb(NativePropertyPlaceConfigurer.SALT, value);
            return encryptedValue == null ? value : NativePropertyPlaceConfigurer.PREFIX + encryptedValue;
        } catch (Exception e) {
            log.warn("can not encrypt sm4 value", e);
        }
        return value;
    }

    public static String sm4Decrypt(String value) {
        if (!StringUtils.startsWith(value, NativePropertyPlaceConfigurer.PREFIX)) {
            return value;
        }
        try {
            String decryptValue = Sm4Crypto.decryptEcb(
                    NativePropertyPlaceConfigurer.SALT,
                    StringUtils.substring(value, NativePropertyPlaceConfigurer.PREFIX.length()));
            return decryptValue == null ? value : decryptValue;
        } catch (Exception e) {
            log.warn("can not decrypt sm4 value", e);
        }
        return value;
    }
}
