package com.github.thundax.modules.auth.persistence.dao;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.github.thundax.common.Constants;
import com.github.thundax.common.cache.CacheDTO;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.auth.dao.MemberAuthSessionRuntimeDao;
import com.github.thundax.modules.auth.entity.MemberAuthSession;
import com.github.thundax.modules.auth.entity.enums.MemberAuthSessionStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

@Repository
@Profile("!test")
public class MemberAuthSessionRuntimeDaoImpl implements MemberAuthSessionRuntimeDao {
    private static final String CACHE_SECTION = Constants.CACHE_PREFIX + "MEMBER_AUTH_SESSION_RUNTIME_";
    private static final String SESSION_PREFIX = CACHE_SECTION + "SESSION_";

    @CreateCache(name = CACHE_SECTION, cacheType = CacheType.REMOTE)
    private Cache<String, MemberAuthSessionCacheDTO> cache;

    @Override
    public MemberAuthSession getById(EntityId id) {
        return toDomain(cache.get(sessionKey(id)));
    }

    @Override
    public void insert(MemberAuthSession authSession, int expireSeconds) {
        Assert.notNull(authSession, "authSession can not be null");
        Assert.notNull(authSession.getId(), "id can not be null");
        if (expireSeconds <= 0) {
            return;
        }
        cache.put(sessionKey(authSession.getId()), toCacheDTO(authSession), expireSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void deleteById(EntityId id) {
        cache.remove(sessionKey(id));
    }

    private String sessionKey(EntityId id) {
        return SESSION_PREFIX + EntityIdCodec.toStringValue(id);
    }

    private static MemberAuthSession toDomain(MemberAuthSessionCacheDTO cacheDTO) {
        if (cacheDTO == null) {
            return null;
        }
        MemberAuthSession authSession = new MemberAuthSession();
        authSession.setId(EntityIdCodec.toDomain(cacheDTO.id));
        authSession.setPrincipalKey(PrincipalKey.of(
                PrincipalType.from(cacheDTO.principalType), EntityIdCodec.toDomain(cacheDTO.principalId)));
        authSession.setIdentityId(EntityIdCodec.toDomain(cacheDTO.identityId));
        authSession.setIdentityType(identityTypeFrom(cacheDTO.identityType));
        authSession.setLoginType(cacheDTO.loginType);
        authSession.setStatus(cacheDTO.status == null ? null : MemberAuthSessionStatus.from(cacheDTO.status));
        authSession.setIssuedAt(cacheDTO.issuedAt);
        authSession.setLastAccessTime(cacheDTO.lastAccessTime);
        authSession.setExpireAt(cacheDTO.expireAt);
        authSession.setLogoutAt(cacheDTO.logoutAt);
        authSession.setInvalidateReason(cacheDTO.invalidateReason);
        return authSession;
    }

    private static MemberAuthSessionCacheDTO toCacheDTO(MemberAuthSession authSession) {
        MemberAuthSessionCacheDTO cacheDTO = new MemberAuthSessionCacheDTO();
        cacheDTO.id = EntityIdCodec.toValue(authSession.getId());
        cacheDTO.principalType =
                authSession.getPrincipalKey().getPrincipalType().value();
        cacheDTO.principalId =
                EntityIdCodec.toValue(authSession.getPrincipalKey().getPrincipalId());
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
        return cacheDTO;
    }

    private static PrincipalIdentityType identityTypeFrom(String identityType) {
        if (identityType == null) {
            return null;
        }
        if (identityType.startsWith("MEMBER_")) {
            return PrincipalIdentityType.from(identityType);
        }
        return PrincipalIdentityType.from(PrincipalType.MEMBER, identityType);
    }

    private static class MemberAuthSessionCacheDTO implements CacheDTO {
        private Long id;
        private String principalType;
        private Long principalId;
        private Long identityId;
        private String identityType;
        private String loginType;
        private String status;
        private Date issuedAt;
        private Date lastAccessTime;
        private Date expireAt;
        private Date logoutAt;
        private String invalidateReason;
    }
}
