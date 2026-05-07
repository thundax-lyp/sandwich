package com.github.thundax.modules.member.utils;

import com.github.thundax.common.utils.RSAUtils;
import com.github.thundax.modules.member.service.SessionCacheSupport;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

@Component
public class RsaSessionUtils {

    private static final String CACHE_RSA_MODULUS = "rsa_modulus";
    private static final String CACHE_RSA_PRIVATE_EXPONENT = "rsa_private_exponent";

    private final SessionCacheSupport sessionCacheSupport;

    public RsaSessionUtils(SessionCacheSupport sessionCacheSupport) {
        this.sessionCacheSupport = sessionCacheSupport;
    }

    public String updateRsaKey(HttpServletRequest request) {
        RSAUtils.ReadableKeyPair keyPair = RSAUtils.generateKeyPair();

        HttpSession session = request.getSession(true);

        sessionCacheSupport.put(
                session.getId(), CACHE_RSA_MODULUS, keyPair.getModulus(), session.getMaxInactiveInterval());
        sessionCacheSupport.put(
                session.getId(),
                CACHE_RSA_PRIVATE_EXPONENT,
                keyPair.getPrivateKeyExponent(),
                session.getMaxInactiveInterval());

        return keyPair.getPublicKey();
    }

    public String decryptRsaValue(HttpServletRequest request, String encryptedValue) {
        HttpSession session = request.getSession(true);

        String modulus = sessionCacheSupport.get(session.getId(), CACHE_RSA_MODULUS, String.class);
        String privateExponent = sessionCacheSupport.get(session.getId(), CACHE_RSA_PRIVATE_EXPONENT, String.class);

        RSAUtils.ReadableKeyPair keyPair = new RSAUtils.ReadableKeyPair(null, modulus, null, privateExponent);
        return RSAUtils.decryptBase64(encryptedValue, keyPair);
    }
}
