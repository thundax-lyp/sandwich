package com.github.thundax.modules.auth.service;

import com.github.thundax.common.arch.LayerPublicApi;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.AccessToken;
import com.github.thundax.modules.auth.exception.InvalidCaptchaException;
import com.github.thundax.modules.auth.service.dto.PreAuthSessionDTO;
import com.github.thundax.modules.auth.service.result.AuthTokenQueryResult;
import com.github.thundax.modules.auth.service.result.AuthTokenRefreshResult;
import com.github.thundax.modules.auth.service.result.OAuth2AuthorizationDecisionResult;
import com.github.thundax.modules.auth.service.result.OAuth2AuthorizationViewResult;
import com.github.thundax.modules.sys.entity.User;
import java.util.List;
import org.springframework.lang.NonNull;

public interface AdminAuthService {

    PreAuthSessionDTO createPreAuthSession() throws ApiException;

    /**
     * 刷新登录令牌 刷新后，refreshToken并未立即消失，而是指向新的Token位置，直到60秒后，此时可能有多个refreshToken指向同一个token。
     * 这样处理是未了避免"于前端的网络延迟而导致refresh丢失"。
     *
     * @param refreshToken 刷新令牌
     * @return 登录令牌
     * @throws InvalidTokenException 无效的refreshToken
     */
    PreAuthSessionDTO refreshPreAuthSession(String refreshToken) throws ApiException;

    void releasePreAuthSession(String loginToken) throws InvalidTokenException;

    String createCaptcha(String loginToken) throws InvalidTokenException;

    String getCaptcha(String loginToken) throws InvalidTokenException, InvalidCaptchaException;

    /**
     * 校验图形验证码
     *
     * @param loginToken 登录令牌
     * @param captcha 验证码
     * @return 正确:true；不正确:false
     * @throws InvalidTokenException token不正确
     * @throws InvalidCaptchaException 验证码并未生成
     */
    boolean validateCaptcha(String loginToken, String captcha) throws InvalidTokenException, InvalidCaptchaException;

    String createSmsValidateCode(String loginToken, String mobile) throws InvalidTokenException;

    String getSmsValidateCode(String loginToken) throws InvalidTokenException, InvalidCaptchaException;

    /**
     * 校验短信验证码
     *
     * @param loginToken 登录令牌
     * @param mobile 手机号码
     * @param validateCode 短信验证码
     * @return 正确:true；不正确:false
     * @throws InvalidTokenException token不正确
     * @throws InvalidCaptchaException 验证码并未生成
     */
    boolean validateSmsValidateCode(String loginToken, String mobile, String validateCode)
            throws InvalidTokenException, InvalidCaptchaException;

    @NonNull
    AccessToken createAccessToken(String userId);

    @NonNull
    AccessToken createAccessToken(String userId, String loginName);

    AccessToken getAccessToken(String token);

    AccessToken getByUserId(String userId);

    boolean validateToken(AccessToken accessToken);

    void activeAccessToken(AccessToken accessToken);

    void deleteAccessToken(AccessToken accessToken);

    AuthTokenQueryResult queryToken(String token);

    AuthTokenRefreshResult refreshAccessToken(String clientId, String refreshToken) throws ApiException;

    OAuth2AuthorizationViewResult authorizeOAuth2(
            String clientId, String redirectUri, List<String> scopes, String state) throws ApiException;

    OAuth2AuthorizationDecisionResult decideOAuth2(
            String clientId,
            String redirectUri,
            List<String> scopes,
            String state,
            String codeChallenge,
            String codeChallengeMethod,
            String userId,
            boolean approved)
            throws ApiException;

    AuthTokenRefreshResult exchangeOAuth2Token(
            String clientId,
            String clientSecret,
            String grantType,
            String redirectUri,
            String authorizationCode,
            String codeVerifier,
            String refreshToken)
            throws ApiException;

    boolean revokeAuthorizationCode(String authorizationCode) throws ApiException;

    boolean revokeOAuth2Token(String clientId, String clientSecret, String token) throws ApiException;

    void invalidateSessionByToken(String token, String reason);

    @LayerPublicApi(reason = "账号状态变化时按用户维度失效在线会话的业务入口")
    int invalidateSessionsByUserId(EntityId userId, String reason);

    /**
     * 账号密码认证。
     *
     * @param loginName 登录名
     * @param plainPassword 明文密码
     * @return 认证通过的用户
     * @throws ApiException 业务异常
     */
    User authenticatePassword(String loginName, String plainPassword) throws ApiException;

    User authenticateSms(String loginToken, String mobile, String validateCode) throws ApiException;

    User authenticateWecom(String code) throws ApiException;

    User authenticateGithub(String code) throws ApiException;

    /**
     * 校验登录密码并处理失败锁定。
     *
     * @param user 用户
     * @param plainPassword 明文密码
     * @throws ApiException 业务异常
     */
    void validatePassword(User user, String plainPassword) throws ApiException;

    String getPrivateKey(String loginToken) throws InvalidTokenException;
}
