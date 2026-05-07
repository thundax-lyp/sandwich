package com.github.thundax.modules.auth.dao;

public interface KeypairPrivateKeyDao {

    void insert(String token, String privateKey, int expiredSeconds);

    String getByToken(String token);
}
