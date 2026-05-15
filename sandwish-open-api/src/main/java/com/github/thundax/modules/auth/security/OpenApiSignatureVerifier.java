package com.github.thundax.modules.auth.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.lang3.StringUtils;

public class OpenApiSignatureVerifier {

    private static final char[] HEX = "0123456789abcdef".toCharArray();
    private static final String SHA_256 = "SHA-256";
    private static final String HMAC_SHA_256 = "HmacSHA256";

    public boolean matchesBodyHash(byte[] body, String expectedHash) {
        return StringUtils.isNotBlank(expectedHash) && constantTimeEquals(sha256Hex(body), expectedHash);
    }

    public boolean verify(String secret, OpenApiCanonicalRequest canonicalRequest, String signature) {
        if (StringUtils.isBlank(secret) || canonicalRequest == null || StringUtils.isBlank(signature)) {
            return false;
        }
        return constantTimeEquals(hmacSha256Hex(secret, canonicalRequest.value()), signature);
    }

    public String sha256Hex(byte[] body) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            return toHex(digest.digest(body == null ? new byte[0] : body));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    public String hmacSha256Hex(String secret, String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA_256);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA_256));
            return toHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("HmacSHA256 is not available", e);
        }
    }

    private static boolean constantTimeEquals(String actual, String expected) {
        if (actual == null || expected == null) {
            return false;
        }
        return MessageDigest.isEqual(
                actual.toLowerCase().getBytes(StandardCharsets.UTF_8),
                expected.toLowerCase().getBytes(StandardCharsets.UTF_8));
    }

    private static String toHex(byte[] bytes) {
        char[] value = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int current = bytes[i] & 0xff;
            value[i * 2] = HEX[current >>> 4];
            value[i * 2 + 1] = HEX[current & 0x0f];
        }
        return new String(value);
    }
}
