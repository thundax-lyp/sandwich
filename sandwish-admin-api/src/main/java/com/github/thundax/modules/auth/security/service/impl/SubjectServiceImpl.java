package com.github.thundax.modules.auth.security.service.impl;

import com.github.thundax.common.Constants;
import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.common.collect.MapUtils;
import com.github.thundax.common.collect.SetUtils;
import com.github.thundax.common.utils.DateUtils;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.common.utils.redis.RedisClient;
import com.github.thundax.modules.auth.security.service.SubjectService;
import com.github.thundax.modules.auth.security.subject.Subject;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.service.impl.MenuServiceImpl;
import com.github.thundax.modules.sys.service.impl.RoleServiceImpl;
import com.github.thundax.modules.sys.utils.UserServiceHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.github.thundax.modules.sys.entity.Menu.*;

/**
 * @author thundax
 */
@Service
public class SubjectServiceImpl implements SubjectService,
        MenuServiceImpl.CacheChangedListener, RoleServiceImpl.CacheChangedListener {

    private static final String CACHE_ = Constants.CACHE_PREFIX + "SECURITY_";

    private static final String CACHE_SUBJECT_SUFFIX = ".subject";
    private static final String CACHE_VERSION_SUFFIX = ".version";

    private static final int EXPIRED_SECONDS = 1800;

    private final Map<String, SubjectVersion> uidVersionMap = MapUtils.newHashMap();
    private final Map<String, Subject> uidSubjectMap = MapUtils.newHashMap();

    private final RedisClient redisClient;

    @Autowired
    public SubjectServiceImpl(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    @Override
    public Subject getSubject(String userId) {
        try {
            String remoteVersion = redisClient.get(CACHE_ + userId + CACHE_VERSION_SUFFIX);
            if (StringUtils.isEmpty(remoteVersion)) {
                return createSubject(userId);
            }

            SubjectVersion localVersion = uidVersionMap.get(userId);
            if (localVersion != null && StringUtils.equals(localVersion.version, remoteVersion)) {
                return uidSubjectMap.get(userId);
            }

            Subject subject = redisClient.get(CACHE_ + userId + CACHE_SUBJECT_SUFFIX, Subject.class);
            if (subject == null) {
                return createSubject(userId);
            }

            uidSubjectMap.put(userId, subject);

            return subject;

        } finally {
            redisClient.expire(CACHE_ + userId + CACHE_VERSION_SUFFIX, EXPIRED_SECONDS - 5);
            redisClient.expire(CACHE_ + userId + CACHE_SUBJECT_SUFFIX, EXPIRED_SECONDS);
        }
    }

    @Override
    public void releaseSubject(String userId) {
        redisClient.delete(ListUtils.newArrayList(
                CACHE_ + userId + CACHE_VERSION_SUFFIX,
                CACHE_ + userId + CACHE_SUBJECT_SUFFIX));
    }

    @Override
    public void reloadAll() {
        redisClient.deleteByPattern(CACHE_ + "*");
    }

    private Subject createSubject(String userId) {
        Subject subject = new Subject();
        subject.setUserId(userId);

        User user = UserServiceHolder.get(userId);
        Assert.notNull(user, "use can not be null");

        subject.setRoleIdentifiers(SetUtils.newHashSet(user.getRoleIdList()));

        Set<String> permissions = SetUtils.newHashSet();
        ListUtils.forEach(UserServiceHolder.findMenuList(user), menu -> {
            if (StringUtils.isNotBlank(menu.getPerms())) {
                for (String permission : StringUtils.split(menu.getPerms(), PERM_SEPARATOR)) {
                    if (!PERM_USER.equals(permission) && !PERM_SUPER.equals(permission) && !PERM_ADMIN.equals(permission)) {
                        permissions.add(permission);
                    }
                }
            }
        });

        // 添加用户权限
        permissions.add(PERM_USER);
        if (user.isSuper()) {
            permissions.add(PERM_SUPER);
            permissions.add(PERM_ADMIN);
        } else if (user.isAdmin()) {
            permissions.add(PERM_ADMIN);
        }

        subject.setPermissions(permissions);

        SubjectVersion version = new SubjectVersion();
        version.version = UUID.randomUUID().toString();
        version.timestamp = System.currentTimeMillis();

        redisClient.set(CACHE_ + userId + CACHE_VERSION_SUFFIX, version);
        redisClient.set(CACHE_ + userId + CACHE_SUBJECT_SUFFIX, subject);

        return subject;
    }

    /**
     * 删除本地过期subject
     */
    @Lazy(value = false)
    @Scheduled(cron = "0 0/8 * * * ?")
    public void deleteExpiredSubject() {
        Set<String> expiredUids = SetUtils.newHashSet();
        uidVersionMap.forEach((uid, version) -> {
            if (version.timestamp + EXPIRED_SECONDS * DateUtils.MILLIS_PER_SECOND < System.currentTimeMillis()) {
                expiredUids.add(uid);
            }
        });

        expiredUids.forEach(uid -> {
            uidVersionMap.remove(uid);
            uidSubjectMap.remove(uid);
        });
    }

    @Override
    public void onMenuCacheChanged() {
        removeAllCache();
    }

    @Override
    public void onRoleCacheChanged() {
        removeAllCache();
    }

    private void removeAllCache() {
        redisClient.deleteByPattern(CACHE_ + "*");
        uidVersionMap.clear();
        uidSubjectMap.clear();
    }

    public static class SubjectVersion implements Serializable {

        private String version;
        private long timestamp;

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
