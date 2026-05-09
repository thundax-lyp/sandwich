package com.github.thundax.modules.auth.persistence.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.alicp.jetcache.Cache;
import com.github.thundax.common.Constants;
import com.github.thundax.modules.auth.entity.PrincipalAuthSession;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalAuthSessionId;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class PrincipalAuthSessionDaoImplTest {
    private static final String SAMPLE_VALUE_NAME = "SAMPLE";

    @Test
    public void shouldStoreTouchAndDeletePrincipalAuthSessionById() throws Exception {
        PrincipalAuthSessionDaoImpl dao = new PrincipalAuthSessionDaoImpl();
        TestCache cache = new TestCache();
        injectCache(dao, cache);

        PrincipalAuthSession session = session();
        dao.insert(session, 70);

        PrincipalAuthSession stored = dao.getById(PrincipalAuthSessionId.of("fa1"));
        assertEquals(PrincipalAuthSessionId.of("fa1"), stored.getId());
        assertEquals(PrincipalType.USER, stored.getPrincipalKey().getPrincipalType());
        assertEquals(Long.valueOf(1001L), stored.getPrincipalKey().getPrincipalId());
        assertEquals("admin-api", stored.getClientId());
        assertEquals(
                new LinkedHashSet<>(Arrays.asList("sys:user:query", "sys:user:update")),
                stored.getValues().get(SAMPLE_VALUE_NAME));
        assertEquals(Long.valueOf(70L), cache.getTtlSeconds(sessionKey("fa1")));

        Date accessTime = new Date(3000L);
        dao.touch(PrincipalAuthSessionId.of("fa1"), accessTime, 80);

        PrincipalAuthSession touched = dao.getById(PrincipalAuthSessionId.of("fa1"));
        assertEquals(accessTime, touched.getLastAccessTime());
        assertEquals(Long.valueOf(80L), cache.getTtlSeconds(sessionKey("fa1")));

        dao.deleteById(PrincipalAuthSessionId.of("fa1"));

        assertNull(dao.getById(PrincipalAuthSessionId.of("fa1")));
    }

    private PrincipalAuthSession session() {
        Map<String, Object> values = new HashMap<>();
        values.put(SAMPLE_VALUE_NAME, new LinkedHashSet<>(Arrays.asList("sys:user:query", "sys:user:update")));
        return PrincipalAuthSession.restore(
                PrincipalAuthSessionId.of("fa1"),
                PrincipalKey.of(PrincipalType.USER, 1001L),
                "admin-api",
                values,
                new Date(1000L),
                new Date(1000L),
                new Date(60000L));
    }

    private String sessionKey(String sessionId) {
        return Constants.CACHE_PREFIX + "PRINCIPAL_AUTH_SESSION_SESSION_" + sessionId;
    }

    private void injectCache(PrincipalAuthSessionDaoImpl dao, TestCache cache) throws Exception {
        Field field = PrincipalAuthSessionDaoImpl.class.getDeclaredField("cache");
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
