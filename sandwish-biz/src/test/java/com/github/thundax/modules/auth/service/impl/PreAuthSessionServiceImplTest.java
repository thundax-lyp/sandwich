package com.github.thundax.modules.auth.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.common.i18n.I18nMessages;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.dao.AccessTokenDao;
import com.github.thundax.modules.auth.dao.LoginFormDao;
import com.github.thundax.modules.auth.entity.AccessToken;
import com.github.thundax.modules.auth.entity.LoginForm;
import com.github.thundax.modules.auth.exception.InvalidCaptchaException;
import com.github.thundax.modules.auth.exception.TooManyLoginRequestException;
import com.github.thundax.modules.auth.exception.TooManyOnlineUserException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.StaticMessageSource;

public class PreAuthSessionServiceImplTest {

    private AuthProperties properties;
    private RecordingLoginFormDao loginFormDao;
    private RecordingAccessTokenDao accessTokenDao;
    private PreAuthSessionServiceImpl service;

    @BeforeClass
    public static void setUpMessages() {
        StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.addMessage("common.exception.invalid-token", Locale.getDefault(), "invalid token");
        messageSource.addMessage("auth.exception.invalid-captcha", Locale.getDefault(), "invalid captcha");
        messageSource.addMessage("auth.exception.too-many-login-request", Locale.getDefault(), "too many login");
        messageSource.addMessage("auth.exception.too-many-online-user", Locale.getDefault(), "too many online");
        new I18nMessages(messageSource);
    }

    @Before
    public void setUp() {
        properties = new AuthProperties();
        properties.setLoginExpiredSeconds(60);
        loginFormDao = new RecordingLoginFormDao();
        accessTokenDao = new RecordingAccessTokenDao();
        service = new PreAuthSessionServiceImpl(properties, loginFormDao, accessTokenDao);
    }

    @Test
    public void shouldCreateLoginFormWhenCapacityAvailable() throws Exception {
        LoginForm form = service.createLoginForm();

        assertNotNull(form.getLoginToken());
        assertEquals(Integer.valueOf(60), form.getExpiredSeconds());
        assertEquals(1, form.getRefreshTokenList().size());
        assertEquals(4, form.getCaptcha().length());
        assertNotNull(form.getPublicKey());
        assertNotNull(form.getPrivateKey());
        assertEquals(form, loginFormDao.getByToken(form.getLoginToken()));
    }

    @Test(expected = TooManyLoginRequestException.class)
    public void shouldRejectLoginFormWhenPreAuthCapacityFull() throws Exception {
        properties.setMaxLoginCount(0);
        loginFormDao.count = 1;

        service.createLoginForm();
    }

    @Test(expected = TooManyOnlineUserException.class)
    public void shouldRejectLoginFormWhenOnlineCapacityFull() throws Exception {
        properties.setMaxOnlineCount(0);
        accessTokenDao.count = 1;

        service.createLoginForm();
    }

    @Test
    public void shouldRefreshLoginForm() throws Exception {
        LoginForm form = service.createLoginForm();
        String oldLoginToken = form.getLoginToken();
        String refreshToken = form.getRefreshTokenList().get(0);

        LoginForm refreshed = service.refreshLoginForm(refreshToken);

        assertNotEquals(oldLoginToken, refreshed.getLoginToken());
        assertEquals(2, refreshed.getRefreshTokenList().size());
        assertEquals(refreshed, loginFormDao.getByToken(refreshed.getLoginToken()));
    }

    @Test(expected = InvalidTokenException.class)
    public void shouldRejectInvalidRefreshToken() throws Exception {
        service.refreshLoginForm("missing-refresh-token");
    }

    @Test
    public void shouldCreateAndValidateCaptcha() throws Exception {
        LoginForm form = service.createLoginForm();

        String captcha = service.createCaptcha(form.getLoginToken());

        assertEquals(captcha, service.getCaptcha(form.getLoginToken()));
        assertTrue(service.validateCaptcha(form.getLoginToken(), captcha));
    }

    @Test(expected = InvalidCaptchaException.class)
    public void shouldRejectNullCaptcha() throws Exception {
        LoginForm form = service.createLoginForm();
        form.setCaptcha("null");

        service.getCaptcha(form.getLoginToken());
    }

    @Test
    public void shouldCreateAndValidateSmsCodeAndClearCaptcha() throws Exception {
        LoginForm form = service.createLoginForm();

        String code = service.createSmsValidateCode(form.getLoginToken(), "13800000000");

        assertNull(form.getCaptcha());
        assertEquals(code, service.getSmsValidateCode(form.getLoginToken()));
        assertTrue(service.validateSmsValidateCode(form.getLoginToken(), "13800000000", code));
    }

    @Test
    public void shouldReturnPrivateKey() throws Exception {
        LoginForm form = service.createLoginForm();

        assertEquals(form.getPrivateKey(), service.getPrivateKey(form.getLoginToken()));
    }

    private static class RecordingLoginFormDao implements LoginFormDao {
        private final Map<String, LoginForm> forms = new HashMap<>();
        private final Map<String, LoginForm> refreshForms = new HashMap<>();
        private int count;

        @Override
        public int count() {
            return count == 0 ? forms.size() : count;
        }

        @Override
        public LoginForm getByToken(String loginToken) {
            return forms.get(loginToken);
        }

        @Override
        public LoginForm getByRefreshToken(String refreshToken) {
            return refreshForms.get(refreshToken);
        }

        @Override
        public void insert(LoginForm form) {
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
        public void updateCaptcha(String token, String captcha) {
            forms.get(token).setCaptcha(captcha);
        }

        @Override
        public void updateSmsValidateCode(String token, String mobile, String validateCode) {
            LoginForm form = forms.get(token);
            form.setMobile(mobile);
            form.setMobileValidateCode(validateCode);
        }
    }

    private static class RecordingAccessTokenDao implements AccessTokenDao {
        private int count;

        @Override
        public int count() {
            return count;
        }

        @Override
        public String getUidByToken(String token) {
            return null;
        }

        @Override
        public AccessToken getByUserId(String userId) {
            return null;
        }

        @Override
        public void insert(AccessToken accessToken) {}

        @Override
        public void active(AccessToken accessToken) {}

        @Override
        public void deleteByToken(String token) {}
    }
}
