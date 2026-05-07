package com.github.thundax.modules.assist.controller;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.common.exception.PermissionDeniedException;
import com.github.thundax.common.security.annotation.PublicApi;
import com.github.thundax.common.web.annotation.WrappedApiController;
import com.github.thundax.modules.assist.assembler.KeypairInterfaceAssembler;
import com.github.thundax.modules.assist.controller.request.KeypairPublicKeyRequest;
import com.github.thundax.modules.assist.controller.response.KeypairPublicKeyResponse;
import com.github.thundax.modules.assist.service.KeypairService;
import com.github.thundax.modules.auth.entity.AccessToken;
import com.github.thundax.modules.auth.service.AdminAuthService;
import com.github.thundax.modules.auth.utils.AuthUtils;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Api(tags = "辅助/公钥与私钥")
@SysLogger(module = {"辅助", "公私钥对"})
@RequestMapping(value = "/api/assist/keypair")
@WrappedApiController
public class KeypairController {

    private final AdminAuthService authService;
    private final KeypairService keypairService;

    @Autowired
    public KeypairController(AdminAuthService authService, KeypairService keypairService) {

        this.authService = authService;
        this.keypairService = keypairService;
    }

    @ApiOperation(value = "获取公钥")
    @PublicApi
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("获取公钥")
    @RequestMapping(value = "public", method = RequestMethod.POST)
    public KeypairPublicKeyResponse publicKey(@Valid @RequestBody KeypairPublicKeyRequest request) throws ApiException {
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
        return KeypairInterfaceAssembler.toPublicKeyResponse(publicKey);
    }
}
