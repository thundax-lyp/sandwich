package com.github.thundax.modules.auth.service;

public interface PasswordService {

    String encrypt(String plainPassword);

    boolean validate(String plainPassword, String encryptedPassword);
}
