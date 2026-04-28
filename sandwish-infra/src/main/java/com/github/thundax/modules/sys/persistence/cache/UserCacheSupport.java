package com.github.thundax.modules.sys.persistence.cache;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.github.thundax.common.Constants;
import com.github.thundax.common.utils.IdGen;
import com.github.thundax.modules.sys.entity.User;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;


@Component
public class UserCacheSupport {

    private static final int OBJECT_EXPIRE_SECONDS = 3600;
    private static final int VERSION_EXPIRE_SECONDS = OBJECT_EXPIRE_SECONDS + 5;
    private static final String CACHE_SECTION = Constants.CACHE_PREFIX + "sys.user";
    private static final String ID_PREFIX = "id_";
    private static final String VERSION_KEY = "version";
    private static final String ROLES_PREFIX = ".roles_";
    private static final String KEY_INDEX = "keys";

    @CreateCache(
            name = CACHE_SECTION,
            cacheType = CacheType.REMOTE,
            expire = OBJECT_EXPIRE_SECONDS,
            timeUnit = TimeUnit.SECONDS)
    private Cache<String, Object> cache;

    @CreateCache(
            name = CACHE_SECTION + ".keys.",
            cacheType = CacheType.REMOTE,
            expire = VERSION_EXPIRE_SECONDS,
            timeUnit = TimeUnit.SECONDS)
    private Cache<String, Set<String>> keyIndexCache;

    public User getById(String id) {
        return (User) cache.get(objectKey(id));
    }

    public void putById(User user) {
        if (user != null && StringUtils.isNotBlank(user.getId())) {
            String key = objectKey(user.getId());
            cache.put(key, user, OBJECT_EXPIRE_SECONDS, TimeUnit.SECONDS);
            rememberKey(key);
        }
    }

    public void removeById(String id) {
        String key = objectKey(id);
        cache.remove(key);
        forgetKey(key);
        touchVersion();
    }

    public void removeAll() {
        Set<String> keys = keyIndexCache.get(KEY_INDEX);
        if (keys != null && !keys.isEmpty()) {
            cache.removeAll(keys);
        }
        keyIndexCache.remove(KEY_INDEX);
        touchVersion();
    }

    @SuppressWarnings("unchecked")
    public List<String> getUserRoleIds(String userId) {
        return (List<String>) cache.get(userRoleIdsKey(userId));
    }

    public void putUserRoleIds(String userId, List<String> roleIds) {
        String key = userRoleIdsKey(userId);
        cache.put(key, roleIds);
        rememberKey(key);
    }

    public void removeUserRoleIds(String userId) {
        String key = userRoleIdsKey(userId);
        cache.remove(key);
        forgetKey(key);
    }

    public String currentVersion() {
        String version = (String) cache.get(versionKey());
        if (StringUtils.isBlank(version)) {
            version = IdGen.uuid();
            cache.put(versionKey(), version);
        }
        return version;
    }

    public void touchVersion() {
        cache.put(versionKey(), IdGen.uuid(), VERSION_EXPIRE_SECONDS, TimeUnit.SECONDS);
    }

    private String objectKey(String id) {
        return CACHE_SECTION + ID_PREFIX + id;
    }

    private String userRoleIdsKey(String userId) {
        return CACHE_SECTION + ROLES_PREFIX + userId;
    }

    private String versionKey() {
        return CACHE_SECTION + VERSION_KEY;
    }

    private void rememberKey(String key) {
        Set<String> keys = keyIndexCache.get(KEY_INDEX);
        if (keys == null) {
            keys = new HashSet<>();
        }
        if (keys.add(key)) {
            keyIndexCache.put(KEY_INDEX, keys, VERSION_EXPIRE_SECONDS, TimeUnit.SECONDS);
        }
    }

    private void forgetKey(String key) {
        Set<String> keys = keyIndexCache.get(KEY_INDEX);
        if (keys == null) {
            return;
        }
        if (keys.remove(key)) {
            keyIndexCache.put(KEY_INDEX, keys, VERSION_EXPIRE_SECONDS, TimeUnit.SECONDS);
        }
    }
}
