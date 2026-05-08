package com.github.thundax.modules.auth.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.common.i18n.I18nMessages;
import com.github.thundax.modules.auth.dao.PreAuthSessionDao;
import com.github.thundax.modules.auth.entity.PreAuthSession;
import com.github.thundax.modules.auth.entity.PreAuthSession.RefreshTokenValue;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionId;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionToken;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.StaticMessageSource;

public class PreAuthSessionServiceImplTest {
    private static final String CAPTCHA_ITEM = "CAPTCHA";

    private RecordingPreAuthSessionDao preAuthSessionDao;
    private PreAuthSessionServiceImpl service;

    @BeforeClass
    public static void setUpMessages() {
        StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.addMessage("common.exception.invalid-token", Locale.getDefault(), "invalid token");
        new I18nMessages(messageSource);
    }

    @Before
    public void setUp() {
        preAuthSessionDao = new RecordingPreAuthSessionDao();
        service = new PreAuthSessionServiceImpl(preAuthSessionDao);
    }

    @Test
    public void shouldCreateAndRefreshPreAuthSession() throws Exception {
        PreAuthSession session = service.create(60);

        assertEquals(session.getId(), service.findIdByToken(session.getToken()));
        assertEquals(session.getId(), service.findIdByRefreshToken(session.getRefreshToken()));

        PreAuthSessionToken oldToken = session.getToken();
        PreAuthSessionToken oldRefreshToken = session.getRefreshToken();

        PreAuthSession refreshed = service.refresh(session.getId(), 60, 60);

        assertEquals(session.getId(), refreshed.getId());
        assertNotEquals(oldToken, refreshed.getToken());
        assertNotEquals(oldRefreshToken, refreshed.getRefreshToken());
        assertNull(service.findIdByToken(oldToken));
        assertEquals(session.getId(), service.findIdByToken(refreshed.getToken()));
        assertEquals(session.getId(), service.findIdByRefreshToken(refreshed.getRefreshToken()));
        assertEquals(session.getId(), service.findIdByRefreshToken(oldRefreshToken));
    }

    @Test
    public void shouldReleasePreAuthSessionById() {
        PreAuthSession session = service.create(60);

        service.release(session.getId());

        assertNull(preAuthSessionDao.getById(session.getId()));
        assertNull(service.findIdByToken(session.getToken()));
        assertNull(service.findIdByRefreshToken(session.getRefreshToken()));
    }

    @Test
    public void shouldManagePreAuthSessionValueWithTtl() throws Exception {
        PreAuthSession session = service.create(60);

        service.upsertValue(session.getId(), CAPTCHA_ITEM, "2345", System.currentTimeMillis() + 60000L);
        assertEquals("2345", service.findValue(session.getId(), CAPTCHA_ITEM));

        service.upsertValue(session.getId(), CAPTCHA_ITEM, "2345", System.currentTimeMillis() - 1L);

        assertNull(service.findValue(session.getId(), CAPTCHA_ITEM));
    }

    @Test(expected = InvalidTokenException.class)
    public void shouldRejectExpiredPreAuthSessionById() throws Exception {
        PreAuthSessionToken refreshToken = PreAuthSessionToken.of("refresh-token-1");
        PreAuthSession session = PreAuthSession.restore(
                PreAuthSessionId.of("session-1"),
                PreAuthSessionToken.of("token-1"),
                java.util.Collections.singletonList(
                        RefreshTokenValue.of(refreshToken, System.currentTimeMillis() - 1L)),
                System.currentTimeMillis() - 1L);
        preAuthSessionDao.insert(session);

        service.getById(session.getId());
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
