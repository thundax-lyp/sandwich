package com.github.thundax.common.jasypt;

import com.github.thundax.common.utils.encrypt.Des;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.StringEncryptor;

/**
 * 为配置中的敏感信息加密
 *
 * @author thundax
 */
public class JasyptStringEncryptor implements StringEncryptor {

    public static final String DEFAULT_PREFIX = "WDIT:";
    public static final String DEFAULT_PASSWORD = "http://www.wdit.com.cn/encryptor";

    private final String prefix;
    private final String password;

    public JasyptStringEncryptor() {
        this.prefix = DEFAULT_PREFIX;
        this.password = DEFAULT_PASSWORD;
    }

    public JasyptStringEncryptor(String prefix, String password) {
        this.prefix = StringUtils.isBlank(prefix) ? DEFAULT_PREFIX : prefix;
        this.password = StringUtils.isBlank(password) ? DEFAULT_PASSWORD : password;
    }

    @Override
    public String encrypt(String message) {
        if (StringUtils.isEmpty(message)) {
            return message;
        }
        return prefix + Des.encrypt(message, password);
    }

    @Override
    public String decrypt(String encryptedMessage) {
        if (!StringUtils.startsWith(encryptedMessage, prefix)) {
            return encryptedMessage;
        }
        return Des.decrypt(encryptedMessage.substring(prefix.length()), password);
    }
}
