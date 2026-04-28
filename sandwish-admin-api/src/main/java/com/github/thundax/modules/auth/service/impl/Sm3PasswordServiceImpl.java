package com.github.thundax.modules.auth.service.impl;

import com.github.thundax.common.codec.Sm3Utils;
import com.github.thundax.modules.auth.service.PasswordService;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import org.apache.commons.lang3.StringUtils;

/**
 * SM3加密
 *
 * @author wdit
 */
public class Sm3PasswordServiceImpl implements PasswordService {

    private static final String PREFIX = "ENC(";
    private static final String SUFFIX = ")";

    private static final String ARG_SEPARATOR = ",";
    private static final int ARG_SALT = 0;
    private static final int ARG_SIZE = 2;

    @Override
    public String encrypt(String plainPassword) {
        return encrypt(plainPassword, generateSalt());
    }

    private String encrypt(String plainPassword, String salt) {
        String encryptedPassword =
                Sm3Utils.sm3Hex((salt + ARG_SEPARATOR + plainPassword).getBytes(StandardCharsets.UTF_8));
        return PREFIX + salt + ARG_SEPARATOR + encryptedPassword + SUFFIX;
    }

    public String generateSalt() {
        return String.format("%x", 0x1000 + new Random().nextInt(0xefff));
    }

    @Override
    public boolean validate(String plainPassword, String encryptedPassword) {
        if (StringUtils.startsWithIgnoreCase(encryptedPassword, PREFIX)
                && StringUtils.endsWithIgnoreCase(encryptedPassword, SUFFIX)) {
            String queryString =
                    encryptedPassword.substring(PREFIX.length(), encryptedPassword.length() - SUFFIX.length());
            String[] args = StringUtils.split(queryString, ARG_SEPARATOR);
            if (args.length != ARG_SIZE) {
                return false;
            }

            String salt = args[ARG_SALT];

            return StringUtils.equals(encrypt(plainPassword, salt), encryptedPassword);
        }

        return StringUtils.equals(Sm3Utils.sm3Hex(plainPassword), encryptedPassword);
    }

    public static void main(String[] argv) {

        Sm3PasswordServiceImpl impl = new Sm3PasswordServiceImpl();
        System.out.println(impl.encrypt("Q1w2e3r$", "fdf7"));
        System.out.println(impl.validate(
                "Q1w2e3r$", "ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)"));

        // "8f5928a2f8b4ead6f91374f1a009a4d620034e5681ac1404e8d5b60730de61eb";
    }
}
