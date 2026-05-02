package com.github.thundax.modules.assist.service;

/**
 * 国密服务
 */
public interface EncryptService {

    String encrypt(String value);

    String decrypt(String encryptedValue);
}
