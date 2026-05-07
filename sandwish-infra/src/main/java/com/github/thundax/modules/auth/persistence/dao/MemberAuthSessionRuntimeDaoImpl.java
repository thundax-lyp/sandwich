package com.github.thundax.modules.auth.persistence.dao;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.github.thundax.common.Constants;
import com.github.thundax.common.cache.CacheDTO;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.auth.dao.MemberAuthSessionRuntimeDao;
import com.github.thundax.modules.auth.entity.MemberAuthSession;
import com.github.thundax.modules.auth.entity.enums.MemberAuthSessionStatus;
import com.github.thundax.modules.member.entity.enums.MemberIdentityType;
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
    public MemberAuthSession getBySessionId(String sessionId) {
        return toDomain(cache.get(SESSION_PREFIX + sessionId));
    }

    @Override
    public void insert(MemberAuthSession authSession, int expireSeconds) {
        Assert.notNull(authSession, "authSession can not be null");
        Assert.hasText(authSession.getSessionId(), "sessionId can not be empty");
        if (expireSeconds <= 0) {
            return;
        }
        cache.put(
                SESSION_PREFIX + authSession.getSessionId(), toCacheDTO(authSession), expireSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void deleteBySessionId(String sessionId) {
        cache.remove(SESSION_PREFIX + sessionId);
    }

    private static MemberAuthSession toDomain(MemberAuthSessionCacheDTO cacheDTO) {
        if (cacheDTO == null) {
            return null;
        }
        MemberAuthSession authSession = new MemberAuthSession();
        authSession.setId(EntityIdCodec.toDomain(cacheDTO.id));
        authSession.setSessionId(cacheDTO.sessionId);
        authSession.setMemberId(EntityIdCodec.toDomain(cacheDTO.memberId));
        authSession.setIdentityId(EntityIdCodec.toDomain(cacheDTO.identityId));
        authSession.setIdentityType(
                cacheDTO.identityType == null ? null : MemberIdentityType.from(cacheDTO.identityType));
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
        cacheDTO.sessionId = authSession.getSessionId();
        cacheDTO.memberId = EntityIdCodec.toValue(authSession.getMemberId());
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

    private static class MemberAuthSessionCacheDTO implements CacheDTO {
        private Long id;
        private String sessionId;
        private Long memberId;
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
