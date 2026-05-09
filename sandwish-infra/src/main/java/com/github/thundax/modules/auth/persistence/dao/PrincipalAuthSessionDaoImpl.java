package com.github.thundax.modules.auth.persistence.dao;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.github.thundax.common.Constants;
import com.github.thundax.common.cache.CacheDTO;
import com.github.thundax.modules.auth.codec.PrincipalAuthSessionIdCodec;
import com.github.thundax.modules.auth.dao.PrincipalAuthSessionDao;
import com.github.thundax.modules.auth.entity.PrincipalAuthSession;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalAuthSessionId;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

@Repository
@Profile("!test")
public class PrincipalAuthSessionDaoImpl implements PrincipalAuthSessionDao {

    private static final String CACHE_SECTION = Constants.CACHE_PREFIX + "PRINCIPAL_AUTH_SESSION_";
    private static final String SESSION_PREFIX = CACHE_SECTION + "SESSION_";

    @CreateCache(name = CACHE_SECTION, cacheType = CacheType.REMOTE)
    private Cache<String, PrincipalAuthSessionCacheDTO> cache;

    @Override
    public PrincipalAuthSession getById(PrincipalAuthSessionId id) {
        return toEntity(cache.get(sessionKey(id)));
    }

    @Override
    public void insert(PrincipalAuthSession session, int expireSeconds) {
        Assert.notNull(session, "session can not be null");
        Assert.notNull(session.getId(), "id can not be null");
        Assert.notNull(session.getPrincipalKey(), "principalKey can not be null");
        Assert.hasText(session.getClientId(), "clientId can not be blank");
        if (expireSeconds <= 0) {
            return;
        }
        cache.put(sessionKey(session.getId()), toCacheDTO(session), expireSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void touch(PrincipalAuthSessionId id, Date accessTime, int expireSeconds) {
        PrincipalAuthSessionCacheDTO cacheDTO = cache.get(sessionKey(id));
        if (cacheDTO == null || expireSeconds <= 0) {
            return;
        }
        cacheDTO.lastAccessTime = accessTime;
        cache.put(sessionKey(id), cacheDTO, expireSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void deleteById(PrincipalAuthSessionId id) {
        cache.remove(sessionKey(id));
    }

    private String sessionKey(PrincipalAuthSessionId id) {
        return SESSION_PREFIX + PrincipalAuthSessionIdCodec.toValue(id);
    }

    private static PrincipalAuthSession toEntity(PrincipalAuthSessionCacheDTO cacheDTO) {
        if (cacheDTO == null) {
            return null;
        }
        return PrincipalAuthSession.restore(
                PrincipalAuthSessionIdCodec.toDomain(cacheDTO.id),
                PrincipalKey.of(PrincipalType.from(cacheDTO.principalType), cacheDTO.principalId),
                cacheDTO.clientId,
                toEntityValues(cacheDTO.values),
                cacheDTO.issuedAt,
                cacheDTO.lastAccessTime,
                cacheDTO.expireAt);
    }

    private static PrincipalAuthSessionCacheDTO toCacheDTO(PrincipalAuthSession session) {
        PrincipalAuthSessionCacheDTO cacheDTO = new PrincipalAuthSessionCacheDTO();
        cacheDTO.id = PrincipalAuthSessionIdCodec.toValue(session.getId());
        cacheDTO.principalType = session.getPrincipalKey().getPrincipalType().value();
        cacheDTO.principalId = session.getPrincipalKey().getPrincipalId();
        cacheDTO.clientId = session.getClientId();
        cacheDTO.values = toCacheValues(session.getValues());
        cacheDTO.issuedAt = session.getIssuedAt();
        cacheDTO.lastAccessTime = session.getLastAccessTime();
        cacheDTO.expireAt = session.getExpireAt();
        return cacheDTO;
    }

    private static Map<String, Object> toEntityValues(Map<String, Object> cacheValues) {
        Map<String, Object> values = new LinkedHashMap<>();
        if (cacheValues == null) {
            return values;
        }
        values.putAll(cacheValues);
        return values;
    }

    private static Map<String, Object> toCacheValues(Map<String, Object> entityValues) {
        Map<String, Object> values = new LinkedHashMap<>();
        if (entityValues == null) {
            return values;
        }
        values.putAll(entityValues);
        return values;
    }

    private static class PrincipalAuthSessionCacheDTO implements CacheDTO {
        private String id;
        private String principalType;
        private Long principalId;
        private String clientId;
        private Map<String, Object> values = new LinkedHashMap<>();
        private Date issuedAt;
        private Date lastAccessTime;
        private Date expireAt;
    }
}
