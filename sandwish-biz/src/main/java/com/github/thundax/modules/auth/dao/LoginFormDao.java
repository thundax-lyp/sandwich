package com.github.thundax.modules.auth.dao;

import com.github.thundax.modules.auth.entity.LoginForm;

public interface LoginFormDao {

    int getLoginCount();

    LoginForm getByToken(String loginToken);

    LoginForm getByRefreshToken(String refreshToken);

    void insert(LoginForm form);

    void deleteByToken(String loginToken);

    boolean tokenExists(String loginToken);

    void updateCaptcha(String token, String captcha);

    void updateSmsValidateCode(String token, String mobile, String validateCode);
}
