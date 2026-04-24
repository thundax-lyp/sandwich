package com.github.thundax.modules.assist.controller;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.common.exception.PermissionDeniedException;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.common.utils.encrypt.Sm2;
import com.github.thundax.common.utils.redis.RedisClient;
import com.github.thundax.common.web.BaseApiController;
import com.github.thundax.modules.assist.api.KeypairServiceApi;
import com.github.thundax.modules.assist.api.vo.PublicKeyVo;
import com.github.thundax.modules.auth.api.vo.AccessTokenVo;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.entity.AccessToken;
import com.github.thundax.modules.auth.service.AuthService;
import com.github.thundax.modules.auth.utils.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Validator;

/**
 * @author wdit
 */
@RestController
public class KeypairApiController extends BaseApiController implements KeypairServiceApi {

    private final AuthService authService;
    private final RedisClient redisClient;
    private final AuthProperties properties;

    private static final int SAFETY_SECONDS = 5;

    @Autowired
    public KeypairApiController(Validator validator, AuthService authService, RedisClient redisClient, AuthProperties properties) {
        super(validator);

        this.authService = authService;
        this.redisClient = redisClient;
        this.properties = properties;
    }


    /**
     * 获取公钥
     *
     * @param token 用户令牌
     * @return 公钥
     * @throws ApiException API异常
     */
    @Override
    public PublicKeyVo publicKey(AccessTokenVo token) throws ApiException {
        if (StringUtils.isEmpty(token.getToken())) {
            throw new InvalidTokenException();
        }

        AccessToken accessToken = authService.getAccessToken(token.getToken());
        if (accessToken == null) {
            throw new InvalidTokenException();
        }

        if (!AuthUtils.validateCheckCode(accessToken.getCheckCode())) {
            throw new PermissionDeniedException();
        }

        Sm2.StringKeyPair keyPair = Sm2.generateKeyPair();
        redisClient.set(Constants.CACHE_PRIVATE_KEY_ + token.getToken(), keyPair.getPrivateKey(),
                properties.getLoginExpiredSeconds() + SAFETY_SECONDS * 2);
        logger.info("privateKey:{}, publicKey:{}", keyPair.getPrivateKey(), keyPair.getPublicKey());
        PublicKeyVo vo = new PublicKeyVo();
        vo.setPublicKey(keyPair.getPublicKey());
        return vo;
    }
}
