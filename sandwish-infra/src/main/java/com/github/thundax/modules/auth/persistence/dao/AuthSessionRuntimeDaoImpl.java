package com.github.thundax.modules.auth.persistence.dao;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.github.thundax.common.Constants;
import com.github.thundax.modules.auth.dao.AuthSessionRuntimeDao;
import com.github.thundax.modules.auth.entity.AuthSession;
import com.github.thundax.modules.auth.persistence.assembler.AuthSessionPersistenceAssembler;
import com.github.thundax.modules.auth.persistence.dataobject.AuthSessionDO;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

@Repository
@Profile("!test")
public class AuthSessionRuntimeDaoImpl implements AuthSessionRuntimeDao {

    private static final String CACHE_SECTION = Constants.CACHE_PREFIX + "AUTH_SESSION_RUNTIME_";

    private static final String TOKEN_PREFIX = CACHE_SECTION + "TOKEN_";

    @CreateCache(name = CACHE_SECTION, cacheType = CacheType.REMOTE)
    private Cache<String, AuthSessionDO> cache;

    @Override
    public AuthSession getByToken(String token) {
        return AuthSessionPersistenceAssembler.toEntity(cache.get(TOKEN_PREFIX + token));
    }

    @Override
    public void insert(AuthSession authSession, int expiredSeconds) {
        Assert.notNull(authSession, "authSession can not be null");
        Assert.hasText(authSession.getToken(), "token can not be empty");
        if (expiredSeconds <= 0) {
            return;
        }
        cache.put(
                TOKEN_PREFIX + authSession.getToken(),
                AuthSessionPersistenceAssembler.toDataObject(authSession),
                expiredSeconds,
                TimeUnit.SECONDS);
    }

    @Override
    public void touch(String token, Date accessTime, int expiredSeconds) {
        AuthSessionDO dataObject = cache.get(TOKEN_PREFIX + token);
        if (dataObject == null || expiredSeconds <= 0) {
            return;
        }
        dataObject.setLastAccessTime(accessTime);
        cache.put(TOKEN_PREFIX + token, dataObject, expiredSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void deleteByToken(String token) {
        cache.remove(TOKEN_PREFIX + token);
    }
}
