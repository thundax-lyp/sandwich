package com.github.thundax.modules.auth.dao;


import com.github.thundax.modules.auth.entity.LoginForm;

/**
 * @author wdit
 */
public interface LoginFormDao {

    /**
     * 获取当前登录用户数
     *
     * @return 当前登录用户数
     */
    int getLoginCount();

    /**
     * 获取LoginToken
     *
     * @param loginToken 令牌
     * @return LoginForm
     */
    LoginForm getByToken(String loginToken);

    /**
     * 获取 LoginForm
     *
     * @param refreshToken 刷新令牌
     * @return LoginForm
     */
    LoginForm getByRefreshToken(String refreshToken);

    /**
     * 保存登录令牌
     *
     * @param form 登录表单
     */
    void save(LoginForm form);

    /**
     * 删除令牌
     *
     * @param loginToken 登录令牌
     */
    void deleteByToken(String loginToken);

    /**
     * 是否存在登录令牌
     *
     * @param loginToken 登录令牌
     * @return 存在:true; 其他:false
     */
    boolean tokenExists(String loginToken);


    /**
     * 更新图形验证码
     *
     * @param token   登录令牌
     * @param captcha 验证码
     */
    void updateCaptcha(String token, String captcha);


    /**
     * 更新短信验证码
     *
     * @param token        登录令牌
     * @param mobile       手机号码
     * @param validateCode 验证码
     */
    void updateSmsValidateCode(String token, String mobile, String validateCode);

}
