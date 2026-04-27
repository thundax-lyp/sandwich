package com.github.thundax.common.utils.encrypt;

import org.bouncycastle.crypto.digests.SM3Digest;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.github.thundax.common.utils.StringUtils.byte2hex;

public class SM3 {

    private static final int DIGEST_LENGTH = 32;

    public static String encrypt(InputStream inputStream) {
        try {
            byte[] output = new byte[DIGEST_LENGTH];

            SM3Digest digest = new SM3Digest();

            BufferedInputStream bis = new BufferedInputStream(inputStream);
            byte[] buffer = new byte[1024];
            int readBytes;
            while ((readBytes = bis.read(buffer)) > 0) {
                digest.update(buffer, 0, readBytes);
            }
            digest.doFinal(output, 0);

            return byte2hex(output);

        } catch (IOException e) {
            return null;
        }
    }

    public static String encrypt(String input) {
        return encrypt(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String encrypt(byte[] input) {
        byte[] output = new byte[DIGEST_LENGTH];

        SM3Digest digest = new SM3Digest();
        digest.update(input, 0, input.length);
        digest.doFinal(output, 0);

        return byte2hex(output);
    }

    public static void main(String[] args) {

        System.out.println(encrypt("13123"));
    }

}
