package com.github.thundax.modules.auth.controller;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.security.annotation.PublicApi;
import com.github.thundax.common.utils.RSAUtils;
import com.github.thundax.modules.auth.assembler.MemberLoginInterfaceAssembler;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.controller.request.MemberAccountLoginRequest;
import com.github.thundax.modules.auth.controller.request.MemberLogoutRequest;
import com.github.thundax.modules.auth.controller.request.MemberRefreshTokenRequest;
import com.github.thundax.modules.auth.controller.request.MemberSmsLoginRequest;
import com.github.thundax.modules.auth.controller.response.MemberLoginFormResponse;
import com.github.thundax.modules.auth.controller.response.MemberLoginStatusResponse;
import com.github.thundax.modules.auth.controller.response.MemberTokenResponse;
import com.github.thundax.modules.auth.entity.PreAuthSession;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionId;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionToken;
import com.github.thundax.modules.auth.security.MemberSecurityContext;
import com.github.thundax.modules.auth.security.MemberSpringPrincipal;
import com.github.thundax.modules.auth.service.MemberAuthService;
import com.github.thundax.modules.auth.service.PreAuthSessionService;
import com.github.thundax.modules.auth.utils.PreAuthCodeHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "会员认证")
@RestController
@RequestMapping(value = "/auth")
@PublicApi
public class LoginController {

    private static final String CAPTCHA_ITEM = "CAPTCHA";
    private static final String PUBLIC_KEY_ITEM = "publicKey";
    private static final String PRIVATE_KEY_ITEM = "privateKey";
    private static final String SMS_MOBILE_ITEM = "SMS_MOBILE";
    private static final String SMS_VALIDATE_CODE_ITEM = "SMS_VALIDATE_CODE";
    private static final String MEMBER_PRIVATE_KEY_SEPARATOR = ":";
    private static final int CAPTCHA_EXPIRED_SECONDS = 60;
    private static final int REFRESH_TOKEN_GRACE_SECONDS = 60;

    private final MemberAuthService memberAuthService;
    private final PreAuthSessionService preAuthSessionService;
    private final AuthProperties authProperties;

    public LoginController(
            MemberAuthService memberAuthService,
            PreAuthSessionService preAuthSessionService,
            AuthProperties authProperties) {
        this.memberAuthService = memberAuthService;
        this.preAuthSessionService = preAuthSessionService;
        this.authProperties = authProperties;
    }

    @ApiOperation(value = "请求预认证会话")
    @PostMapping("pre-auth-session")
    public MemberLoginFormResponse preAuthSession() throws ApiException {
        return MemberLoginInterfaceAssembler.toLoginFormResponse(createPreAuthSession());
    }

    @ApiOperation(value = "刷新预认证会话")
    @PostMapping("pre-auth-session/refresh")
    public MemberLoginFormResponse refreshPreAuthSession(@Valid @RequestBody MemberRefreshTokenRequest request)
            throws ApiException {
        return MemberLoginInterfaceAssembler.toLoginFormResponse(refreshPreAuthSession(request.getRefreshToken()));
    }

    @ApiOperation(value = "账号密码登录")
    @PostMapping("login")
    public MemberTokenResponse loginAccount(@Valid @RequestBody MemberAccountLoginRequest request) throws ApiException {
        PreAuthSessionToken token = PreAuthSessionToken.of(request.getLoginToken());
        if (!validateCaptcha(token, request.getCaptcha())) {
            throw new ApiException("图形验证码错误");
        }
        String password = decryptRsaValue(token, request.getPassword());
        preAuthSessionService.release(requireSessionIdByToken(request.getLoginToken()));
        return MemberLoginInterfaceAssembler.toTokenResponse(
                memberAuthService.loginAccount(request.getAccount(), password));
    }

    @ApiOperation(value = "短信登录")
    @PostMapping("login/sms")
    public MemberTokenResponse loginSms(@Valid @RequestBody MemberSmsLoginRequest request) throws ApiException {
        PreAuthSessionToken token = PreAuthSessionToken.of(request.getLoginToken());
        if (!validateSmsValidateCode(token, request.getMobile(), request.getValidateCode())) {
            throw new ApiException("短信验证码错误");
        }
        preAuthSessionService.release(requireSessionIdByToken(request.getLoginToken()));
        return MemberLoginInterfaceAssembler.toTokenResponse(memberAuthService.loginSms(request.getMobile()));
    }

    @ApiOperation(value = "刷新 access token")
    @PostMapping("token/refresh")
    public MemberTokenResponse refreshAccessToken(@Valid @RequestBody MemberRefreshTokenRequest request)
            throws ApiException {
        return MemberLoginInterfaceAssembler.toTokenResponse(
                memberAuthService.refreshAccessToken(request.getRefreshToken()));
    }

    @ApiOperation(value = "登录状态")
    @GetMapping("login")
    public MemberLoginStatusResponse login() {
        MemberSpringPrincipal principal = MemberSecurityContext.getPrincipal();
        return MemberLoginInterfaceAssembler.toLoginStatusResponse(principal);
    }

    @ApiOperation(value = "检查登录状态")
    @GetMapping("check-login")
    public MemberLoginStatusResponse checkLogin() {
        MemberSpringPrincipal principal = MemberSecurityContext.getPrincipal();
        return MemberLoginInterfaceAssembler.toLoginStatusResponse(principal);
    }

    @ApiOperation(value = "登出")
    @PostMapping("logout")
    public MemberLoginStatusResponse logout(@Valid @RequestBody MemberLogoutRequest request) throws ApiException {
        memberAuthService.logout(request.getAccessToken());
        return MemberLoginInterfaceAssembler.toLogoutResponse();
    }

    private PreAuthSession createPreAuthSession() throws ApiException {
        if (preAuthSessionService.count() > authProperties.getMaxLoginCount()) {
            throw new ApiException("登录请求过多");
        }
        PreAuthSession session = preAuthSessionService.create(authProperties.getLoginExpiredSeconds());
        writeCaptcha(session.getId(), PreAuthCodeHelper.generateCaptcha());
        RSAUtils.ReadableKeyPair keyPair = RSAUtils.generateKeyPair();
        preAuthSessionService.upsertValue(
                session.getId(), PUBLIC_KEY_ITEM, keyPair.getPublicKey(), session.getExpiredAt());
        preAuthSessionService.upsertValue(
                session.getId(), PRIVATE_KEY_ITEM, memberPrivateKeyValue(keyPair), session.getExpiredAt());
        return preAuthSessionService.getById(session.getId());
    }

    private PreAuthSession refreshPreAuthSession(String refreshToken) throws ApiException {
        PreAuthSession session = preAuthSessionService.refresh(
                requireSessionIdByRefreshToken(refreshToken),
                authProperties.getLoginExpiredSeconds(),
                REFRESH_TOKEN_GRACE_SECONDS);
        writeCaptcha(session.getId(), PreAuthCodeHelper.generateCaptcha());
        return session;
    }

    private boolean validateCaptcha(PreAuthSessionToken token, String captcha) throws ApiException {
        if (StringUtils.isNotBlank(authProperties.getWhiteCaptcha())
                && StringUtils.equals(authProperties.getWhiteCaptcha(), captcha)) {
            return true;
        }
        return StringUtils.equals(captcha, preAuthSessionService.findValue(requireSessionId(token), CAPTCHA_ITEM));
    }

    private boolean validateSmsValidateCode(PreAuthSessionToken token, String mobile, String validateCode)
            throws ApiException {
        if (StringUtils.isNotBlank(authProperties.getWhiteCaptcha())
                && StringUtils.equals(authProperties.getWhiteCaptcha(), validateCode)) {
            return true;
        }
        PreAuthSessionId sessionId = requireSessionId(token);
        return StringUtils.equals(preAuthSessionService.findValue(sessionId, SMS_MOBILE_ITEM), mobile)
                && StringUtils.equals(preAuthSessionService.findValue(sessionId, SMS_VALIDATE_CODE_ITEM), validateCode);
    }

    private String decryptRsaValue(PreAuthSessionToken token, String encryptedValue) throws ApiException {
        String privateKey = preAuthSessionService.findValue(requireSessionId(token), PRIVATE_KEY_ITEM);
        if (StringUtils.isBlank(privateKey)) {
            throw new ApiException("登录表单密钥已失效");
        }
        String[] privateKeyParts = StringUtils.split(privateKey, MEMBER_PRIVATE_KEY_SEPARATOR);
        if (privateKeyParts == null || privateKeyParts.length != 2) {
            throw new ApiException("登录表单密钥已失效");
        }
        RSAUtils.ReadableKeyPair keyPair =
                new RSAUtils.ReadableKeyPair(null, privateKeyParts[0], null, privateKeyParts[1]);
        return RSAUtils.decryptBase64(encryptedValue, keyPair);
    }

    private void writeCaptcha(PreAuthSessionId sessionId, String captcha) throws ApiException {
        preAuthSessionService.upsertValue(
                sessionId, CAPTCHA_ITEM, captcha, System.currentTimeMillis() + CAPTCHA_EXPIRED_SECONDS * 1000L);
    }

    private String memberPrivateKeyValue(RSAUtils.ReadableKeyPair keyPair) {
        return keyPair.getModulus() + MEMBER_PRIVATE_KEY_SEPARATOR + keyPair.getPrivateKeyExponent();
    }

    private PreAuthSessionId requireSessionIdByToken(String token) throws ApiException {
        return requireSessionId(PreAuthSessionToken.of(token));
    }

    private PreAuthSessionId requireSessionId(PreAuthSessionToken token) throws ApiException {
        PreAuthSessionId sessionId = preAuthSessionService.findIdByToken(token);
        if (sessionId == null) {
            throw new ApiException("登录表单已失效");
        }
        return sessionId;
    }

    private PreAuthSessionId requireSessionIdByRefreshToken(String refreshToken) throws ApiException {
        PreAuthSessionId sessionId = preAuthSessionService.findIdByRefreshToken(PreAuthSessionToken.of(refreshToken));
        if (sessionId == null) {
            throw new ApiException("登录表单已失效");
        }
        return sessionId;
    }
}
