package com.github.thundax.modules.member.utils;

import com.github.thundax.common.utils.RSAUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author wdit
 */
public class RsaSessionUtils {

    private static final String CACHE_RSA_MODULUS = "rsa_modulus";
    private static final String CACHE_RSA_PRIVATE_EXPONENT = "rsa_private_exponent";

    /**
     * 注册RSA modules & privateKey
     */
    public static String updateRsaKey(HttpServletRequest request) {
        RSAUtils.ReadableKeyPair keyPair = RSAUtils.generateKeyPair();

        HttpSession session = request.getSession(true);

        SessionCacheServiceHolder.put(session.getId(), CACHE_RSA_MODULUS, keyPair.getModulus(), session.getMaxInactiveInterval());
        SessionCacheServiceHolder.put(session.getId(), CACHE_RSA_PRIVATE_EXPONENT, keyPair.getPrivateKeyExponent(), session.getMaxInactiveInterval());

        return keyPair.getPublicKey();
    }

    /**
     * RSA解码
     */
    public static String decryptRsaValue(HttpServletRequest request, String encryptedValue) {
        HttpSession session = request.getSession(true);

        String modulus = SessionCacheServiceHolder.get(session.getId(), CACHE_RSA_MODULUS, String.class);
        String privateExponent = SessionCacheServiceHolder.get(session.getId(), CACHE_RSA_PRIVATE_EXPONENT, String.class);

        RSAUtils.ReadableKeyPair keyPair = new RSAUtils.ReadableKeyPair(null, modulus, null, privateExponent);
        return RSAUtils.decryptBase64(encryptedValue, keyPair);
    }

}
