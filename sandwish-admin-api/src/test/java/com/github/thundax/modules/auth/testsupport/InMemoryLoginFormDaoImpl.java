package com.github.thundax.modules.auth.testsupport;

import com.github.thundax.modules.auth.dao.LoginFormDao;
import com.github.thundax.modules.auth.entity.LoginForm;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("test")
@Repository
public class InMemoryLoginFormDaoImpl implements LoginFormDao {

    private final Map<String, LoginForm> forms = new LinkedHashMap<>();
    private final Map<String, String> refreshTokens = new LinkedHashMap<>();

    @Override
    public int getLoginCount() {
        return forms.size();
    }

    @Override
    public LoginForm getByToken(String loginToken) {
        return forms.get(loginToken);
    }

    @Override
    public LoginForm getByRefreshToken(String refreshToken) {
        String loginToken = refreshTokens.get(refreshToken);
        return loginToken == null ? null : getByToken(loginToken);
    }

    @Override
    public void insert(LoginForm form) {
        forms.put(form.getLoginToken(), form);
        for (String refreshToken : form.getRefreshTokenList()) {
            refreshTokens.put(refreshToken, form.getLoginToken());
        }
    }

    @Override
    public void deleteByToken(String loginToken) {
        LoginForm form = forms.remove(loginToken);
        if (form != null) {
            for (String refreshToken : form.getRefreshTokenList()) {
                refreshTokens.remove(refreshToken);
            }
        }
    }

    @Override
    public boolean tokenExists(String loginToken) {
        return forms.containsKey(loginToken);
    }

    @Override
    public void updateCaptcha(String token, String captcha) {
        LoginForm form = forms.get(token);
        if (form != null) {
            form.setCaptcha(captcha);
        }
    }

    @Override
    public void updateSmsValidateCode(String token, String mobile, String validateCode) {
        LoginForm form = forms.get(token);
        if (form != null) {
            form.setMobile(mobile);
            form.setMobileValidateCode(validateCode);
        }
    }
}
