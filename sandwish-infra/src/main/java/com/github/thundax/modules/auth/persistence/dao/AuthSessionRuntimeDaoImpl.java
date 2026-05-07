package com.github.thundax.modules.auth.persistence.dao;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.github.thundax.common.Constants;
import com.github.thundax.common.cache.CacheDTO;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.auth.dao.AuthSessionRuntimeDao;
import com.github.thundax.modules.auth.entity.AuthSession;
import com.github.thundax.modules.auth.entity.enums.AuthSessionStatus;
import com.github.thundax.modules.sys.entity.enums.UserIdentityType;
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
    private Cache<String, AuthSessionCacheDTO> cache;

    @Override
    public AuthSession getByToken(String token) {
        return toDomain(cache.get(TOKEN_PREFIX + token));
    }

    @Override
    public void insert(AuthSession authSession, int expiredSeconds) {
        Assert.notNull(authSession, "authSession can not be null");
        Assert.hasText(authSession.getToken(), "token can not be empty");
        if (expiredSeconds <= 0) {
            return;
        }
        cache.put(TOKEN_PREFIX + authSession.getToken(), toCacheDTO(authSession), expiredSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void touch(String token, Date accessTime, int expiredSeconds) {
        AuthSessionCacheDTO cacheDTO = cache.get(TOKEN_PREFIX + token);
        if (cacheDTO == null || expiredSeconds <= 0) {
            return;
        }
        cacheDTO.lastAccessTime = accessTime;
        cache.put(TOKEN_PREFIX + token, cacheDTO, expiredSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void deleteByToken(String token) {
        cache.remove(TOKEN_PREFIX + token);
    }

    private static AuthSession toDomain(AuthSessionCacheDTO cacheDTO) {
        if (cacheDTO == null) {
            return null;
        }
        AuthSession authSession = new AuthSession();
        authSession.setId(EntityIdCodec.toDomain(cacheDTO.id));
        authSession.setSessionId(cacheDTO.sessionId);
        authSession.setToken(cacheDTO.token);
        authSession.setUserId(EntityIdCodec.toDomain(cacheDTO.userId));
        authSession.setIdentityId(EntityIdCodec.toDomain(cacheDTO.identityId));
        authSession.setIdentityType(
                cacheDTO.identityType == null ? null : UserIdentityType.from(cacheDTO.identityType));
        authSession.setLoginType(cacheDTO.loginType);
        authSession.setStatus(cacheDTO.status == null ? null : AuthSessionStatus.from(cacheDTO.status));
        authSession.setIssuedAt(cacheDTO.issuedAt);
        authSession.setLastAccessTime(cacheDTO.lastAccessTime);
        authSession.setExpireAt(cacheDTO.expireAt);
        authSession.setLogoutAt(cacheDTO.logoutAt);
        authSession.setInvalidateReason(cacheDTO.invalidateReason);
        authSession.setCreateDate(cacheDTO.createDate);
        authSession.setCreateUserId(cacheDTO.createUserId);
        authSession.setUpdateDate(cacheDTO.updateDate);
        authSession.setUpdateUserId(cacheDTO.updateUserId);
        return authSession;
    }

    private static AuthSessionCacheDTO toCacheDTO(AuthSession authSession) {
        AuthSessionCacheDTO cacheDTO = new AuthSessionCacheDTO();
        cacheDTO.id = EntityIdCodec.toValue(authSession.getId());
        cacheDTO.sessionId = authSession.getSessionId();
        cacheDTO.token = authSession.getToken();
        cacheDTO.userId = EntityIdCodec.toValue(authSession.getUserId());
        cacheDTO.identityId = EntityIdCodec.toValue(authSession.getIdentityId());
        cacheDTO.identityType = authSession.getIdentityType() == null
                ? null
                : authSession.getIdentityType().value();
        cacheDTO.loginType = authSession.getLoginType();
        cacheDTO.status =
                authSession.getStatus() == null ? null : authSession.getStatus().value();
        cacheDTO.issuedAt = authSession.getIssuedAt();
        cacheDTO.lastAccessTime = authSession.getLastAccessTime();
        cacheDTO.expireAt = authSession.getExpireAt();
        cacheDTO.logoutAt = authSession.getLogoutAt();
        cacheDTO.invalidateReason = authSession.getInvalidateReason();
        cacheDTO.createDate = authSession.getCreateDate();
        cacheDTO.createUserId = authSession.getCreateUserId();
        cacheDTO.updateDate = authSession.getUpdateDate();
        cacheDTO.updateUserId = authSession.getUpdateUserId();
        return cacheDTO;
    }

    private static class AuthSessionCacheDTO implements CacheDTO {
        private String id;
        private String sessionId;
        private String token;
        private String userId;
        private String identityId;
        private String identityType;
        private String loginType;
        private String status;
        private Date issuedAt;
        private Date lastAccessTime;
        private Date expireAt;
        private Date logoutAt;
        private String invalidateReason;
        private Date createDate;
        private String createUserId;
        private Date updateDate;
        private String updateUserId;
    }
}
