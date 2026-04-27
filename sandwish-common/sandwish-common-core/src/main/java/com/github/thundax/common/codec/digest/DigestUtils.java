package com.github.thundax.common.codec.digest;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;
import org.bouncycastle.crypto.digests.SM3Digest;

/**
 * 数字签名工具类
 *
 * @author wdit
 */
public class DigestUtils {

    public static byte[] sm3(final byte[] data) {
        return Sm3.encrypt(data);
    }

    public static byte[] sm3(final InputStream data) throws IOException {
        return Sm3.encrypt(data);
    }

    public static byte[] sm3(final String data) {
        return sm3(StringUtils.getBytesUtf8(data));
    }

    public static String sm3Hex(final byte[] data) {
        return Hex.encodeHexString(sm3(data));
    }

    public static String sm3Hex(final InputStream data) throws IOException {
        return Hex.encodeHexString(sm3(data));
    }

    public static String sm3Hex(final String data) {
        return Hex.encodeHexString(sm3(data));
    }

    private static class Sm3 {

        private static final int DIGEST_LENGTH = 32;

        public static byte[] encrypt(InputStream inputStream) throws IOException {
            byte[] output = new byte[DIGEST_LENGTH];

            SM3Digest digest = new SM3Digest();

            BufferedInputStream bis = new BufferedInputStream(inputStream);
            byte[] buffer = new byte[1024];
            int readBytes;
            while ((readBytes = bis.read(buffer)) > 0) {
                digest.update(buffer, 0, readBytes);
            }
            digest.doFinal(output, 0);

            return output;
        }

        public static byte[] encrypt(byte[] input) {
            byte[] output = new byte[DIGEST_LENGTH];

            SM3Digest digest = new SM3Digest();
            digest.update(input, 0, input.length);
            digest.doFinal(output, 0);

            return output;
        }

        public static byte[] encrypt(String text) {
            return encrypt(text.getBytes(StandardCharsets.UTF_8));
        }
    }
}
