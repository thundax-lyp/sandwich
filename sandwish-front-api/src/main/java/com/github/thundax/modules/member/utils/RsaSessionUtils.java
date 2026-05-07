package com.github.thundax.modules.member.utils;

import com.github.thundax.common.utils.RSAUtils;
import com.github.thundax.modules.member.dao.MemberLoginFormDao;
import com.github.thundax.modules.member.entity.MemberLoginForm;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class RsaSessionUtils {

    private static final String PRIVATE_KEY_SEPARATOR = ":";

    private final MemberLoginFormDao memberLoginFormDao;

    public RsaSessionUtils(MemberLoginFormDao memberLoginFormDao) {
        this.memberLoginFormDao = memberLoginFormDao;
    }

    public String updateRsaKey(String loginToken) {
        RSAUtils.ReadableKeyPair keyPair = RSAUtils.generateKeyPair();
        memberLoginFormDao.updateKeyPair(loginToken, keyPair.getPublicKey(), privateKeyValue(keyPair));
        return keyPair.getPublicKey();
    }

    public String decryptRsaValue(String loginToken, String encryptedValue) {
        if (StringUtils.isBlank(loginToken) || StringUtils.isBlank(encryptedValue)) {
            return StringUtils.EMPTY;
        }
        MemberLoginForm form = memberLoginFormDao.getByToken(loginToken);
        if (form == null || StringUtils.isBlank(form.getPrivateKey())) {
            return StringUtils.EMPTY;
        }

        String[] privateKeyParts = StringUtils.split(form.getPrivateKey(), PRIVATE_KEY_SEPARATOR);
        if (privateKeyParts == null || privateKeyParts.length != 2) {
            return StringUtils.EMPTY;
        }
        RSAUtils.ReadableKeyPair keyPair =
                new RSAUtils.ReadableKeyPair(null, privateKeyParts[0], null, privateKeyParts[1]);
        return RSAUtils.decryptBase64(encryptedValue, keyPair);
    }

    private String privateKeyValue(RSAUtils.ReadableKeyPair keyPair) {
        return keyPair.getModulus() + PRIVATE_KEY_SEPARATOR + keyPair.getPrivateKeyExponent();
    }
}
