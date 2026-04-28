package com.github.thundax.modules.assist.dao;

public interface KeypairPrivateKeyDao {

    void insert(String token, String privateKey, int expiredSeconds);

    String getByToken(String token);
}
