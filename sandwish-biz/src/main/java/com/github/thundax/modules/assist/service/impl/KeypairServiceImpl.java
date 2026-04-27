package com.github.thundax.modules.assist.service.impl;

import com.github.thundax.common.utils.encrypt.Sm2;
import com.github.thundax.modules.assist.dao.KeypairPrivateKeyDao;
import com.github.thundax.modules.assist.service.KeypairService;
import com.github.thundax.modules.auth.config.AuthProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

/**
 * 公私钥对服务实现。
 */
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
        Sm2.StringKeyPair keyPair = Sm2.generateKeyPair();
        keypairPrivateKeyDao.save(token, keyPair.getPrivateKey(),
                properties.getLoginExpiredSeconds() + SAFETY_SECONDS * 2);
        return keyPair.getPublicKey();
    }

    @Override
    public String getPrivateKey(String token) {
        return keypairPrivateKeyDao.getByToken(token);
    }
}
