package com.github.thundax.modules.auth.service;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.modules.auth.entity.AccessToken;
import com.github.thundax.modules.auth.entity.LoginForm;
import com.github.thundax.modules.auth.exception.InvalidCaptchaException;
import com.github.thundax.modules.auth.exception.TooManyLoginRequestException;
import com.github.thundax.modules.auth.exception.TooManyOnlineUserException;
import com.github.thundax.modules.sys.entity.User;
import org.springframework.lang.NonNull;

public interface AuthService {

    LoginForm createLoginForm() throws TooManyLoginRequestException, TooManyOnlineUserException;

    /**
     * 刷新登录令牌 刷新后，refreshToken并未立即消失，而是指向新的Token位置，直到60秒后，此时可能有多个refreshToken指向同一个token。
     * 这样处理是未了避免"于前端的网络延迟而导致refresh丢失"。
     *
     * @param refreshToken 刷新令牌
     * @return 登录令牌
     * @throws InvalidTokenException 无效的refreshToken
     */
    LoginForm refreshLoginForm(String refreshToken) throws InvalidTokenException;

    void deleteLoginForm(String loginToken) throws InvalidTokenException;

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

    AccessToken getAccessToken(String token);

    AccessToken getByUserId(String userId);

    boolean validateToken(AccessToken accessToken);

    void activeAccessToken(AccessToken accessToken);

    void deleteAccessToken(AccessToken accessToken);

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
