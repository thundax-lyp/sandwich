package com.github.thundax.modules.auth.persistence.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import com.alicp.jetcache.Cache;
import com.github.thundax.common.Constants;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.auth.entity.AuthSession;
import com.github.thundax.modules.auth.entity.enums.AuthSessionStatus;
import com.github.thundax.modules.sys.entity.enums.UserIdentityType;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class AuthSessionRuntimeDaoImplTest {

    @Test
    public void shouldStoreTouchAndDeleteRuntimeSessionByToken() throws Exception {
        AuthSessionRuntimeDaoImpl dao = new AuthSessionRuntimeDaoImpl();
        TestCache cache = new TestCache();
        injectCache(dao, cache);

        AuthSession session = session();
        dao.insert(session, 70);

        AuthSession stored = dao.getByToken("token-1");
        assertEquals("session-1", stored.getSessionId());
        assertSame(AuthSessionStatus.ACTIVE, stored.getStatus());
        assertEquals(Long.valueOf(70L), cache.getTtlSeconds(runtimeKey("token-1")));

        Date accessTime = new Date(3000L);
        dao.touch("token-1", accessTime, 80);

        AuthSession touched = dao.getByToken("token-1");
        assertEquals(accessTime, touched.getLastAccessTime());
        assertEquals(Long.valueOf(80L), cache.getTtlSeconds(runtimeKey("token-1")));

        dao.deleteByToken("token-1");

        assertNull(dao.getByToken("token-1"));
    }

    private AuthSession session() {
        AuthSession session = new AuthSession();
        session.setId(EntityIdCodec.toDomain("session-db-1"));
        session.setSessionId("session-1");
        session.setToken("token-1");
        session.setUserId(EntityIdCodec.toDomain("u1"));
        session.setIdentityId(EntityIdCodec.toDomain("identity-1"));
        session.setIdentityType(UserIdentityType.ACCOUNT);
        session.setLoginType("PASSWORD");
        session.setStatus(AuthSessionStatus.ACTIVE);
        session.setIssuedAt(new Date(1000L));
        session.setLastAccessTime(new Date(1000L));
        session.setExpireAt(new Date(60000L));
        return session;
    }

    private String runtimeKey(String token) {
        return Constants.CACHE_PREFIX + "AUTH_SESSION_RUNTIME_TOKEN_" + token;
    }

    private void injectCache(AuthSessionRuntimeDaoImpl dao, TestCache cache) throws Exception {
        Field field = AuthSessionRuntimeDaoImpl.class.getDeclaredField("cache");
        field.setAccessible(true);
        field.set(dao, cache.proxy());
    }

    private static class TestCache {
        private final Map<String, Object> values = new HashMap<>();
        private final Map<String, Long> ttlSeconds = new HashMap<>();

        private Cache<String, Object> proxy() {
            return (Cache<String, Object>) Proxy.newProxyInstance(
                    Cache.class.getClassLoader(), new Class[] {Cache.class}, (proxy, method, args) -> {
                        if ("get".equals(method.getName())) {
                            return values.get(args[0]);
                        }
                        if ("put".equals(method.getName())) {
                            values.put((String) args[0], args[1]);
                            if (args.length == 4 && TimeUnit.SECONDS == args[3]) {
                                ttlSeconds.put((String) args[0], (Long) args[2]);
                            }
                            return null;
                        }
                        if ("remove".equals(method.getName())) {
                            values.remove(args[0]);
                            ttlSeconds.remove(args[0]);
                            return true;
                        }
                        if ("close".equals(method.getName())) {
                            return null;
                        }
                        if ("unwrap".equals(method.getName())) {
                            return null;
                        }
                        if ("config".equals(method.getName())) {
                            return null;
                        }
                        return null;
                    });
        }

        private Long getTtlSeconds(String key) {
            return ttlSeconds.get(key);
        }
    }
}
