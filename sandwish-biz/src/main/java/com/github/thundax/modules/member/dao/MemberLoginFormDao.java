package com.github.thundax.modules.member.dao;

import com.github.thundax.modules.member.entity.MemberLoginForm;

public interface MemberLoginFormDao {

    int count();

    MemberLoginForm getByToken(String loginToken);

    MemberLoginForm getByRefreshToken(String refreshToken);

    void insert(MemberLoginForm form);

    void deleteByToken(String loginToken);

    boolean tokenExists(String loginToken);

    void updateCaptcha(String loginToken, String captcha);

    void updateSmsValidateCode(String loginToken, String mobile, String validateCode);

    void updateEmailValidateCode(String loginToken, String email, String validateCode);

    void updateKeyPair(String loginToken, String publicKey, String privateKey);
}
