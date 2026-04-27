package com.github.thundax.modules.assist.controller;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.common.exception.PermissionDeniedException;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.common.utils.encrypt.Sm2;
import com.github.thundax.common.utils.redis.RedisClient;
import com.github.thundax.common.web.BaseApiController;
import com.github.thundax.modules.assist.assembler.KeypairInterfaceAssembler;
import com.github.thundax.modules.assist.api.KeypairServiceApi;
import com.github.thundax.modules.assist.request.KeypairPublicKeyRequest;
import com.github.thundax.modules.assist.response.KeypairPublicKeyResponse;
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
    private final KeypairInterfaceAssembler keypairInterfaceAssembler;

    private static final int SAFETY_SECONDS = 5;

    @Autowired
    public KeypairApiController(Validator validator,
                                AuthService authService,
                                RedisClient redisClient,
                                AuthProperties properties,
                                KeypairInterfaceAssembler keypairInterfaceAssembler) {
        super(validator);

        this.authService = authService;
        this.redisClient = redisClient;
        this.properties = properties;
        this.keypairInterfaceAssembler = keypairInterfaceAssembler;
    }


    /**
     * 获取公钥
     *
     * @param request 公钥获取请求
     * @return 公钥
     * @throws ApiException API异常
    */
    @Override
    public KeypairPublicKeyResponse publicKey(KeypairPublicKeyRequest request) throws ApiException {
        validate(request);
        if (StringUtils.isEmpty(request.getToken())) {
            throw new InvalidTokenException();
        }

        AccessToken accessToken = authService.getAccessToken(request.getToken());
        if (accessToken == null) {
            throw new InvalidTokenException();
        }

        if (!AuthUtils.validateCheckCode(accessToken.getCheckCode())) {
            throw new PermissionDeniedException();
        }

        Sm2.StringKeyPair keyPair = Sm2.generateKeyPair();
        redisClient.set(Constants.CACHE_PRIVATE_KEY_ + request.getToken(), keyPair.getPrivateKey(),
                properties.getLoginExpiredSeconds() + SAFETY_SECONDS * 2);
        logger.info("privateKey:{}, publicKey:{}", keyPair.getPrivateKey(), keyPair.getPublicKey());
        return keypairInterfaceAssembler.toPublicKeyResponse(keyPair.getPublicKey());
    }
}
