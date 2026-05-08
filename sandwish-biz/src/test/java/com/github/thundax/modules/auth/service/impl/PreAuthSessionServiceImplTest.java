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
import com.github.thundax.modules.auth.dao.MemberLoginFormDao;
import com.github.thundax.modules.auth.entity.AccessToken;
import com.github.thundax.modules.auth.entity.LoginForm;
import com.github.thundax.modules.auth.entity.MemberLoginForm;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.exception.InvalidCaptchaException;
import com.github.thundax.modules.auth.exception.TooManyLoginRequestException;
import com.github.thundax.modules.auth.exception.TooManyOnlineUserException;
import com.github.thundax.modules.auth.service.dto.PreAuthSessionDTO;
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
    private RecordingMemberLoginFormDao memberLoginFormDao;
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
        memberLoginFormDao = new RecordingMemberLoginFormDao();
        accessTokenDao = new RecordingAccessTokenDao();
        service = new PreAuthSessionServiceImpl(properties, loginFormDao, accessTokenDao, memberLoginFormDao);
    }

    @Test
    public void shouldCreateLoginFormWhenCapacityAvailable() throws Exception {
        PreAuthSessionDTO session = service.createPreAuthSession(PrincipalType.USER);

        assertNotNull(session.getLoginToken());
        assertEquals(Integer.valueOf(60), session.getExpiredSeconds());
        assertEquals(1, session.getRefreshTokenList().size());
        assertNotNull(session.getPublicKey());
        assertNotNull(loginFormDao.getByToken(session.getLoginToken()).getPrivateKey());
        assertEquals(
                4, loginFormDao.getByToken(session.getLoginToken()).getCaptcha().length());
    }

    @Test(expected = TooManyLoginRequestException.class)
    public void shouldRejectLoginFormWhenPreAuthCapacityFull() throws Exception {
        properties.setMaxLoginCount(0);
        loginFormDao.count = 1;

        service.createPreAuthSession(PrincipalType.USER);
    }

    @Test(expected = TooManyOnlineUserException.class)
    public void shouldRejectLoginFormWhenOnlineCapacityFull() throws Exception {
        properties.setMaxOnlineCount(0);
        accessTokenDao.count = 1;

        service.createPreAuthSession(PrincipalType.USER);
    }

    @Test
    public void shouldRefreshLoginForm() throws Exception {
        PreAuthSessionDTO session = service.createPreAuthSession(PrincipalType.USER);
        String oldLoginToken = session.getLoginToken();
        String refreshToken = session.getRefreshTokenList().get(0);

        PreAuthSessionDTO refreshed = service.refreshPreAuthSession(PrincipalType.USER, refreshToken);

        assertNotEquals(oldLoginToken, refreshed.getLoginToken());
        assertEquals(2, refreshed.getRefreshTokenList().size());
        assertNotNull(loginFormDao.getByToken(refreshed.getLoginToken()));
    }

    @Test(expected = InvalidTokenException.class)
    public void shouldRejectInvalidRefreshToken() throws Exception {
        service.refreshPreAuthSession(PrincipalType.USER, "missing-refresh-token");
    }

    @Test
    public void shouldCreateAndValidateCaptcha() throws Exception {
        PreAuthSessionDTO session = service.createPreAuthSession(PrincipalType.USER);

        String captcha = service.createCaptcha(PrincipalType.USER, session.getLoginToken());

        assertEquals(captcha, service.getCaptcha(PrincipalType.USER, session.getLoginToken()));
        assertTrue(service.validateCaptcha(PrincipalType.USER, session.getLoginToken(), captcha));
    }

    @Test(expected = InvalidCaptchaException.class)
    public void shouldRejectNullCaptcha() throws Exception {
        PreAuthSessionDTO session = service.createPreAuthSession(PrincipalType.USER);
        loginFormDao.getByToken(session.getLoginToken()).setCaptcha("null");

        service.getCaptcha(PrincipalType.USER, session.getLoginToken());
    }

    @Test
    public void shouldCreateAndValidateSmsCodeAndClearCaptcha() throws Exception {
        PreAuthSessionDTO session = service.createPreAuthSession(PrincipalType.USER);

        String code = service.createSmsValidateCode(PrincipalType.USER, session.getLoginToken(), "13800000000");

        assertNull(loginFormDao.getByToken(session.getLoginToken()).getCaptcha());
        assertEquals(code, service.getSmsValidateCode(PrincipalType.USER, session.getLoginToken()));
        assertTrue(service.validateSmsValidateCode(PrincipalType.USER, session.getLoginToken(), "13800000000", code));
    }

    @Test
    public void shouldReturnPrivateKey() throws Exception {
        PreAuthSessionDTO session = service.createPreAuthSession(PrincipalType.USER);
        LoginForm form = loginFormDao.getByToken(session.getLoginToken());

        assertEquals(form.getPrivateKey(), service.getPrivateKey(PrincipalType.USER, session.getLoginToken()));
    }

    @Test
    public void shouldCreateAndRefreshPreAuthSessionForMember() throws Exception {
        PreAuthSessionDTO session = service.createPreAuthSession(PrincipalType.MEMBER);
        String oldLoginToken = session.getLoginToken();

        PreAuthSessionDTO refreshed = service.refreshPreAuthSession(
                PrincipalType.MEMBER, session.getRefreshTokenList().get(0));

        assertNotEquals(oldLoginToken, refreshed.getLoginToken());
        assertEquals(2, refreshed.getRefreshTokenList().size());
        assertNotNull(memberLoginFormDao.getByToken(refreshed.getLoginToken()));
    }

    @Test
    public void shouldStoreCodesInPreAuthSessionForMember() throws Exception {
        PreAuthSessionDTO session = service.createPreAuthSession(PrincipalType.MEMBER);

        String captcha = service.createCaptcha(PrincipalType.MEMBER, session.getLoginToken());
        assertTrue(service.validateCaptcha(PrincipalType.MEMBER, session.getLoginToken(), captcha));

        String smsCode = service.createSmsValidateCode(PrincipalType.MEMBER, session.getLoginToken(), "13800000000");
        String emailCode =
                service.createEmailValidateCode(PrincipalType.MEMBER, session.getLoginToken(), "member@example.com");

        assertTrue(
                service.validateSmsValidateCode(PrincipalType.MEMBER, session.getLoginToken(), "13800000000", smsCode));
        assertTrue(service.validateEmailValidateCode(
                PrincipalType.MEMBER, session.getLoginToken(), "member@example.com", emailCode));
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

    private static class RecordingMemberLoginFormDao implements MemberLoginFormDao {
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
}
