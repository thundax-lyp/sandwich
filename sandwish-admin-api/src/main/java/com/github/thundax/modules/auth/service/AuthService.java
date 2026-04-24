package com.github.thundax.modules.auth.service;

import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.modules.auth.entity.AccessToken;
import com.github.thundax.modules.auth.entity.LoginForm;
import com.github.thundax.modules.auth.exception.InvalidCaptchaException;
import com.github.thundax.modules.auth.exception.TooManyLoginRequestException;
import com.github.thundax.modules.auth.exception.TooManyOnlineUserException;
import com.github.thundax.modules.sys.entity.User;
import org.springframework.lang.NonNull;

/**
 * AuthService
 *
 * @author wdit
 */
public interface AuthService {

    /**
     * 创建登录令牌
     *
     * @return 登录令牌
     * @throws TooManyLoginRequestException 登录请求过多
     * @throws TooManyOnlineUserException   在线用户过多
     */
    LoginForm createLoginForm() throws TooManyLoginRequestException, TooManyOnlineUserException;


    /**
     * 刷新登录令牌
     * 刷新后，refreshToken并未立即消失，而是指向新的Token位置，直到60秒后，此时可能有多个refreshToken指向同一个token。
     * 这样处理是未了避免"于前端的网络延迟而导致refresh丢失"。
     *
     * @param refreshToken 刷新令牌
     * @return 登录令牌
     * @throws InvalidTokenException 无效的refreshToken
     */
    LoginForm refreshLoginForm(String refreshToken) throws InvalidTokenException;


    /**
     * 删除登录令牌
     *
     * @param loginToken 登录令牌
     * @throws InvalidTokenException 无效的token
     */
    void deleteLoginForm(String loginToken) throws InvalidTokenException;


    /**
     * 创建验证码
     *
     * @param loginToken 登录令牌
     * @return 验证码
     * @throws InvalidTokenException token不正确
     */
    String createCaptcha(String loginToken) throws InvalidTokenException;

    /**
     * 获取当前验证码
     *
     * @param loginToken 登录令牌
     * @return captcha
     * @throws InvalidTokenException   token不正确
     * @throws InvalidCaptchaException 验证码并未生成
     */
    String getCaptcha(String loginToken) throws InvalidTokenException, InvalidCaptchaException;

    /**
     * 校验图形验证码
     *
     * @param loginToken 登录令牌
     * @param captcha    验证码
     * @return 正确:true；不正确:false
     * @throws InvalidTokenException   token不正确
     * @throws InvalidCaptchaException 验证码并未生成
     */
    boolean validateCaptcha(String loginToken, String captcha) throws InvalidTokenException, InvalidCaptchaException;


    /**
     * 创建短信验证码
     *
     * @param loginToken 登录令牌
     * @param mobile     手机号码
     * @return 短信验证码
     * @throws InvalidTokenException token不正确
     */
    String createSmsValidateCode(String loginToken, String mobile) throws InvalidTokenException;


    /**
     * 获取当前短信验证码
     *
     * @param loginToken 登录令牌
     * @return captcha
     * @throws InvalidTokenException   token不正确
     * @throws InvalidCaptchaException 验证码并未生成
     */
    String getSmsValidateCode(String loginToken) throws InvalidTokenException, InvalidCaptchaException;


    /**
     * 校验短信验证码
     *
     * @param loginToken   登录令牌
     * @param mobile       手机号码
     * @param validateCode 短信验证码
     * @return 正确:true；不正确:false
     * @throws InvalidTokenException   token不正确
     * @throws InvalidCaptchaException 验证码并未生成
     */
    boolean validateSmsValidateCode(String loginToken, String mobile, String validateCode) throws InvalidTokenException, InvalidCaptchaException;


    /**
     * 创建JWToken
     *
     * @param userId userId
     * @return 成功：JWToken对象；失败：null
     */
    @NonNull
    AccessToken createAccessToken(String userId);

    /**
     * 获取 accessToken
     *
     * @param token token
     * @return AccessToken
     */
    AccessToken getAccessToken(String token);

    /**
     * 获取 accessToken
     *
     * @param userId userId
     * @return AccessToken
     */
    AccessToken findByUserId(String userId);

    /**
     * 校验 accessToken
     *
     * @param accessToken accessToken
     * @return true if success
     */
    boolean validateToken(AccessToken accessToken);

    /**
     * 登出
     *
     * @param accessToken accessToken
     */
    void activeAccessToken(AccessToken accessToken);

    /**
     * 登出
     *
     * @param accessToken accessToken
     */
    void deleteAccessToken(AccessToken accessToken);

    /**
     * 校验登录密码并处理失败锁定。
     *
     * @param user 用户
     * @param plainPassword 明文密码
     */
    void validatePassword(User user, String plainPassword);

    /**
     * 获取 PrivateKey
     *
     * @param loginToken 登录令牌
     * @return PrivateKey
     * @throws InvalidTokenException token不正确
     */
    String getPrivateKey(String loginToken) throws InvalidTokenException;

}
