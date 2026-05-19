package com.github.thundax.modules.auth.service.impl;

import static org.junit.Assert.*;

import com.github.thundax.common.exception.BizException;
import com.github.thundax.modules.auth.configure.CaptchaWhitelistProperties;
import com.github.thundax.modules.auth.dao.PreAuthSessionDao;
import com.github.thundax.modules.auth.entity.PreAuthSession;
import com.github.thundax.modules.auth.entity.PreAuthSession.RefreshTokenValue;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionId;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionToken;
import com.github.thundax.modules.auth.service.command.CreatePreAuthSessionCommand;
import com.github.thundax.modules.auth.service.command.RefreshPreAuthSessionCommand;
import com.github.thundax.modules.auth.service.command.ReleasePreAuthSessionCommand;
import com.github.thundax.modules.auth.service.command.UpsertPreAuthSessionValueCommand;
import com.github.thundax.modules.auth.service.query.PreAuthSessionValueQuery;
import com.github.thundax.modules.auth.service.query.PreAuthSessionValueValidateQuery;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class PreAuthSessionServiceImplTest {
    private static final String CAPTCHA_ITEM = "CAPTCHA";

    private RecordingPreAuthSessionDao preAuthSessionDao;
    private PreAuthSessionServiceImpl service;

    @Before
    public void setUp() {
        preAuthSessionDao = new RecordingPreAuthSessionDao();
        service = new PreAuthSessionServiceImpl(preAuthSessionDao, CaptchaWhitelistProperties.disabled());
    }

    @Test
    public void shouldCreateAndRefreshPreAuthSession() throws Exception {
        PreAuthSession session = service.create(new CreatePreAuthSessionCommand(60));

        assertEquals(session.getId(), service.getIdByToken(session.getToken()));
        assertEquals(session.getId(), service.getIdByRefreshToken(session.getRefreshToken()));

        PreAuthSessionToken oldToken = session.getToken();
        PreAuthSessionToken oldRefreshToken = session.getRefreshToken();

        PreAuthSession refreshed = service.refresh(new RefreshPreAuthSessionCommand(session.getId(), 60, 60));

        assertEquals(session.getId(), refreshed.getId());
        assertNotEquals(oldToken, refreshed.getToken());
        assertNotEquals(oldRefreshToken, refreshed.getRefreshToken());
        assertNull(service.getIdByToken(oldToken));
        assertEquals(session.getId(), service.getIdByToken(refreshed.getToken()));
        assertEquals(session.getId(), service.getIdByRefreshToken(refreshed.getRefreshToken()));
        assertEquals(session.getId(), service.getIdByRefreshToken(oldRefreshToken));
    }

    @Test
    public void shouldReleasePreAuthSessionById() {
        PreAuthSession session = service.create(new CreatePreAuthSessionCommand(60));

        service.release(new ReleasePreAuthSessionCommand(session.getId()));

        assertNull(preAuthSessionDao.getById(session.getId()));
        assertNull(service.getIdByToken(session.getToken()));
        assertNull(service.getIdByRefreshToken(session.getRefreshToken()));
    }

    @Test
    public void shouldManagePreAuthSessionValueWithTtl() throws Exception {
        PreAuthSession session = service.create(new CreatePreAuthSessionCommand(60));

        service.upsertValue(new UpsertPreAuthSessionValueCommand(
                session.getId(), CAPTCHA_ITEM, "2345", System.currentTimeMillis() + 60000L));
        assertEquals("2345", service.getValue(new PreAuthSessionValueQuery(session.getId(), CAPTCHA_ITEM)));

        service.upsertValue(new UpsertPreAuthSessionValueCommand(
                session.getId(), CAPTCHA_ITEM, "2345", System.currentTimeMillis() - 1L));

        assertNull(service.getValue(new PreAuthSessionValueQuery(session.getId(), CAPTCHA_ITEM)));
    }

    @Test
    public void shouldMatchCaptchaWhitelistWhenSwitchEnabled() {
        service = new PreAuthSessionServiceImpl(
                preAuthSessionDao, CaptchaWhitelistProperties.of(true, Arrays.asList("6666", "8888")));
        PreAuthSession session = service.create(new CreatePreAuthSessionCommand(60));

        assertTrue(service.existsValidatedValue(new PreAuthSessionValueValidateQuery(
                PreAuthSessionId.of("missing-session"), CAPTCHA_ITEM, "6666", null, null)));
        assertTrue(service.existsValidatedValue(new PreAuthSessionValueValidateQuery(
                PreAuthSessionId.of("missing-session"), CAPTCHA_ITEM, "8888", null, null)));
        assertFalse(service.existsValidatedValue(
                new PreAuthSessionValueValidateQuery(session.getId(), CAPTCHA_ITEM, "1234", null, null)));
    }

    @Test
    public void shouldNotMatchCaptchaWhitelistWhenSwitchDisabled() {
        service = new PreAuthSessionServiceImpl(
                preAuthSessionDao, CaptchaWhitelistProperties.of(false, Arrays.asList("6666", "8888")));
        PreAuthSession session = service.create(new CreatePreAuthSessionCommand(60));

        assertFalse(service.existsValidatedValue(
                new PreAuthSessionValueValidateQuery(session.getId(), CAPTCHA_ITEM, "6666", null, null)));
    }

    @Test
    public void shouldBindCaptchaWhitelistFromCommaSeparatedValues() {
        CaptchaWhitelistProperties properties = new CaptchaWhitelistProperties();

        properties.setWhitelistEnabled(true);
        properties.setWhitelistValues("6666,8888");

        assertTrue(properties.matches("6666"));
        assertTrue(properties.matches("8888"));
    }

    @Test
    public void shouldIgnoreCaptchaWhitelistWhenSwitchDisabledInEnvironment() {
        CaptchaWhitelistProperties properties = new CaptchaWhitelistProperties();

        properties.setWhitelistEnabled(false);
        properties.setWhitelistValues("6666,8888");

        assertFalse(properties.matches("6666"));
    }

    @Test(expected = BizException.class)
    public void shouldRejectExpiredPreAuthSessionById() throws Exception {
        PreAuthSessionToken refreshToken = PreAuthSessionToken.of("refresh-token-1");
        PreAuthSession session = PreAuthSession.restore(
                PreAuthSessionId.of("session-1"),
                PreAuthSessionToken.of("token-1"),
                java.util.Collections.singletonList(
                        RefreshTokenValue.of(refreshToken, System.currentTimeMillis() - 1L)),
                System.currentTimeMillis() - 1L);
        preAuthSessionDao.insert(session);

        service.get(session.getId());
    }

    private static class RecordingPreAuthSessionDao implements PreAuthSessionDao {
        private final Map<String, PreAuthSession> sessions = new HashMap<>();
        private final Map<String, String> tokenIndex = new HashMap<>();
        private final Map<String, String> refreshTokenIndex = new HashMap<>();

        @Override
        public int count() {
            return sessions.size();
        }

        @Override
        public PreAuthSession getById(PreAuthSessionId id) {
            return id == null ? null : sessions.get(id.asString());
        }

        @Override
        public PreAuthSessionId getByToken(PreAuthSessionToken token) {
            return token == null ? null : PreAuthSessionId.ofNullable(tokenIndex.get(token.asString()));
        }

        @Override
        public PreAuthSessionId getByRefreshToken(PreAuthSessionToken refreshToken) {
            return refreshToken == null
                    ? null
                    : PreAuthSessionId.ofNullable(refreshTokenIndex.get(refreshToken.asString()));
        }

        @Override
        public void insert(PreAuthSession session) {
            put(session);
        }

        @Override
        public void update(PreAuthSession session) {
            PreAuthSession oldSession = getById(session.getId());
            if (oldSession != null) {
                removeIndexesBySessionId(session.getId());
            }
            put(session);
        }

        @Override
        public void deleteById(PreAuthSessionId id) {
            PreAuthSession session = sessions.remove(id.asString());
            if (session != null) {
                removeIndexes(session);
            }
        }

        private void put(PreAuthSession session) {
            sessions.put(session.getId().asString(), session);
            tokenIndex.put(session.getToken().asString(), session.getId().asString());
            long now = System.currentTimeMillis();
            for (RefreshTokenValue refreshToken : session.refreshTokenValues()) {
                if (refreshToken.getExpiredAt() > now) {
                    refreshTokenIndex.put(
                            refreshToken.getToken().asString(), session.getId().asString());
                }
            }
        }

        private void removeIndexes(PreAuthSession session) {
            tokenIndex.remove(session.getToken().asString());
            for (RefreshTokenValue refreshToken : session.refreshTokenValues()) {
                refreshTokenIndex.remove(refreshToken.getToken().asString());
            }
        }

        private void removeIndexesBySessionId(PreAuthSessionId id) {
            tokenIndex.entrySet().removeIf(entry -> id.asString().equals(entry.getValue()));
            refreshTokenIndex.entrySet().removeIf(entry -> id.asString().equals(entry.getValue()));
        }
    }
}
