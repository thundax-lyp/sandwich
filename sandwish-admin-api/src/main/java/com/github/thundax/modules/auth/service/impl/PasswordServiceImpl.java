package com.github.thundax.modules.auth.service.impl;

import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.common.utils.encrypt.Md5;
import com.github.thundax.modules.auth.service.PasswordService;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Service;

/** @author wdit */
@Service
public class PasswordServiceImpl implements PasswordService {

    @Override
    public String encrypt(String plainPassword) {
        return Md5.encrypt(plainPassword.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean validate(String plainPassword, String encryptedPassword) {
        return StringUtils.equals(encrypt(plainPassword), encryptedPassword);
    }

    public static void main(String[] argv) {
        PasswordServiceImpl service = new PasswordServiceImpl();
        System.out.println(service.encrypt("wdit@123"));
        System.out.println(service.encrypt("wdit@123"));
    }
}
