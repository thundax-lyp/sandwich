package com.github.thundax.modules.assist.service.impl;

import com.github.thundax.common.utils.EncryptUtils;
import com.github.thundax.modules.assist.service.EncryptService;
import org.apache.commons.lang3.StringUtils;

/**
 * 国密服务默认实现
 */
public class DefaultEncryptServiceImpl implements EncryptService {

    @Override
    public String encrypt(String value) {
        if (StringUtils.isEmpty(value)) {
            return "";
        }

        return EncryptUtils.sm4Encrypt(value);
    }

    @Override
    public String decrypt(String encryptedValue) {
        if (StringUtils.isEmpty(encryptedValue)) {
            return "";
        }
        return EncryptUtils.sm4Decrypt(encryptedValue);
    }
}
