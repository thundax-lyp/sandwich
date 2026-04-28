package com.github.thundax.modules.assist.controller;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.common.exception.PermissionDeniedException;
import com.github.thundax.common.web.BaseApiController;
import com.github.thundax.modules.assist.api.KeypairServiceApi;
import com.github.thundax.modules.assist.assembler.KeypairInterfaceAssembler;
import com.github.thundax.modules.assist.request.KeypairPublicKeyRequest;
import com.github.thundax.modules.assist.response.KeypairPublicKeyResponse;
import com.github.thundax.modules.assist.service.KeypairService;
import com.github.thundax.modules.auth.entity.AccessToken;
import com.github.thundax.modules.auth.service.AuthService;
import com.github.thundax.modules.auth.utils.AuthUtils;
import javax.validation.Validator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/** 公私钥接口入口。 */
@RestController
public class KeypairApiController extends BaseApiController implements KeypairServiceApi {

    private final AuthService authService;
    private final KeypairService keypairService;
    private final KeypairInterfaceAssembler keypairInterfaceAssembler;

    @Autowired
    public KeypairApiController(
            Validator validator,
            AuthService authService,
            KeypairService keypairService,
            KeypairInterfaceAssembler keypairInterfaceAssembler) {
        super(validator);

        this.authService = authService;
        this.keypairService = keypairService;
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

        String publicKey = keypairService.createPublicKey(request.getToken());
        return keypairInterfaceAssembler.toPublicKeyResponse(publicKey);
    }
}
