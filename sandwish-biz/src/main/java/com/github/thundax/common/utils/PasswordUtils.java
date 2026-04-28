package com.github.thundax.common.utils;

import com.github.thundax.common.utils.encrypt.Md5;
import java.nio.charset.StandardCharsets;

public class PasswordUtils {

    public static String encryptPassword(String plainPassword) {
        return Md5.encrypt(plainPassword.getBytes(StandardCharsets.UTF_8));
    }
}
