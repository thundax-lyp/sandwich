package com.github.thundax.modules.auth.service.impl;

import com.github.thundax.common.utils.encrypt.Sm2Helper;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.dao.KeypairPrivateKeyDao;
import com.github.thundax.modules.auth.service.KeypairService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties(AuthProperties.class)
public class KeypairServiceImpl implements KeypairService {

    private static final int SAFETY_SECONDS = 5;

    private final KeypairPrivateKeyDao keypairPrivateKeyDao;
    private final AuthProperties properties;

    public KeypairServiceImpl(KeypairPrivateKeyDao keypairPrivateKeyDao, AuthProperties properties) {
        this.keypairPrivateKeyDao = keypairPrivateKeyDao;
        this.properties = properties;
    }

    @Override
    public String createPublicKey(String token) {
        Sm2Helper.StringKeyPair keyPair = Sm2Helper.generateKeyPair();
        keypairPrivateKeyDao.insert(
                token, keyPair.getPrivateKey(), properties.getLoginExpiredSeconds() + SAFETY_SECONDS * 2);
        return keyPair.getPublicKey();
    }

    @Override
    public String getPrivateKey(String token) {
        return keypairPrivateKeyDao.getByToken(token);
    }
}
