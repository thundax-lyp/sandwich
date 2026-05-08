package com.github.thundax.modules.auth.testsupport;

import com.github.thundax.modules.auth.dao.MemberLoginFormDao;
import com.github.thundax.modules.auth.entity.MemberLoginForm;
import java.util.HashMap;
import java.util.Map;

public class InMemoryMemberLoginFormDaoImpl implements MemberLoginFormDao {

    private final Map<String, MemberLoginForm> forms = new HashMap<>();
    private final Map<String, MemberLoginForm> refreshForms = new HashMap<>();

    @Override
    public int count() {
        return forms.size();
    }

    @Override
    public MemberLoginForm getByToken(String loginToken) {
        return forms.get(loginToken);
    }

    @Override
    public MemberLoginForm getByRefreshToken(String refreshToken) {
        return refreshForms.get(refreshToken);
    }

    @Override
    public void insert(MemberLoginForm form) {
        forms.put(form.getLoginToken(), form);
        for (String refreshToken : form.getRefreshTokenList()) {
            refreshForms.put(refreshToken, form);
        }
    }

    @Override
    public void deleteByToken(String loginToken) {
        forms.remove(loginToken);
    }

    @Override
    public boolean tokenExists(String loginToken) {
        return forms.containsKey(loginToken);
    }

    @Override
    public void updateCaptcha(String loginToken, String captcha) {
        forms.get(loginToken).setCaptcha(captcha);
    }

    @Override
    public void updateSmsValidateCode(String loginToken, String mobile, String validateCode) {
        MemberLoginForm form = forms.get(loginToken);
        form.setMobile(mobile);
        form.setMobileValidateCode(validateCode);
    }

    @Override
    public void updateEmailValidateCode(String loginToken, String email, String validateCode) {
        MemberLoginForm form = forms.get(loginToken);
        form.setEmail(email);
        form.setEmailValidateCode(validateCode);
    }

    @Override
    public void updateKeyPair(String loginToken, String publicKey, String privateKey) {
        MemberLoginForm form = forms.get(loginToken);
        form.setPublicKey(publicKey);
        form.setPrivateKey(privateKey);
    }
}
