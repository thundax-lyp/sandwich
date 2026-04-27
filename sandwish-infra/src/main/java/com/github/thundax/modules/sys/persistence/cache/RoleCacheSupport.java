package com.github.thundax.modules.sys.persistence.cache;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.github.thundax.common.Constants;
import com.github.thundax.common.utils.IdGen;
import com.github.thundax.modules.sys.entity.Role;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/** 角色缓存支撑。 */
@Component
public class RoleCacheSupport {

    private static final int OBJECT_EXPIRE_SECONDS = 3600;
    private static final int VERSION_EXPIRE_SECONDS = OBJECT_EXPIRE_SECONDS + 5;
    private static final String CACHE_SECTION = Constants.CACHE_PREFIX + "sys.role.";
    private static final String ID_PREFIX = "id_";
    private static final String VERSION_KEY = "version";
    private static final String USERS_PREFIX = "users_";
    private static final String MENUS_PREFIX = "menus_";
    private static final String KEY_INDEX = "keys";

    @CreateCache(
            name = CACHE_SECTION,
            cacheType = CacheType.REMOTE,
            expire = OBJECT_EXPIRE_SECONDS,
            timeUnit = TimeUnit.SECONDS)
    private Cache<String, Object> cache;

    @CreateCache(
            name = CACHE_SECTION + "keys.",
            cacheType = CacheType.REMOTE,
            expire = VERSION_EXPIRE_SECONDS,
            timeUnit = TimeUnit.SECONDS)
    private Cache<String, Set<String>> keyIndexCache;

    public Role getById(String id) {
        return (Role) cache.get(objectKey(id));
    }

    public void putById(Role role) {
        if (role != null && StringUtils.isNotBlank(role.getId())) {
            String key = objectKey(role.getId());
            cache.put(key, role, OBJECT_EXPIRE_SECONDS, TimeUnit.SECONDS);
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
    public List<String> getRoleUserIds(String roleId) {
        return (List<String>) cache.get(roleUserIdsKey(roleId));
    }

    public void putRoleUserIds(String roleId, List<String> userIds) {
        String key = roleUserIdsKey(roleId);
        cache.put(key, userIds);
        rememberKey(key);
    }

    public void removeRoleUserIds(String roleId) {
        String key = roleUserIdsKey(roleId);
        cache.remove(key);
        forgetKey(key);
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoleMenuIds(String roleId) {
        return (List<String>) cache.get(roleMenuIdsKey(roleId));
    }

    public void putRoleMenuIds(String roleId, List<String> menuIds) {
        String key = roleMenuIdsKey(roleId);
        cache.put(key, menuIds);
        rememberKey(key);
    }

    public void removeRoleMenuIds(String roleId) {
        String key = roleMenuIdsKey(roleId);
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

    private String roleUserIdsKey(String roleId) {
        return CACHE_SECTION + USERS_PREFIX + roleId;
    }

    private String roleMenuIdsKey(String roleId) {
        return CACHE_SECTION + MENUS_PREFIX + roleId;
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
