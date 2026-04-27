package com.github.thundax.modules.sys.persistence.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.thundax.common.Constants;
import com.github.thundax.common.utils.IdGen;
import org.apache.commons.lang3.StringUtils;
import com.github.thundax.common.utils.redis.RedisClient;
import com.github.thundax.modules.sys.entity.Role;
import java.util.List;
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

    private final RedisClient redisClient;

    public RoleCacheSupport(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    public Role getById(String id) {
        return redisClient.get(objectKey(id), Role.class);
    }

    public void putById(Role role) {
        if (role != null && StringUtils.isNotBlank(role.getId())) {
            redisClient.set(objectKey(role.getId()), role, OBJECT_EXPIRE_SECONDS);
        }
    }

    public void removeById(String id) {
        redisClient.delete(objectKey(id));
        touchVersion();
    }

    public void removeAll() {
        redisClient.deleteByPattern(CACHE_SECTION + "*");
        touchVersion();
    }

    public List<String> getRoleUserIds(String roleId) {
        return redisClient.get(roleUserIdsKey(roleId), new TypeReference<List<String>>() {});
    }

    public void putRoleUserIds(String roleId, List<String> userIds) {
        redisClient.set(roleUserIdsKey(roleId), userIds);
    }

    public void removeRoleUserIds(String roleId) {
        redisClient.delete(roleUserIdsKey(roleId));
    }

    public List<String> getRoleMenuIds(String roleId) {
        return redisClient.get(roleMenuIdsKey(roleId), new TypeReference<List<String>>() {});
    }

    public void putRoleMenuIds(String roleId, List<String> menuIds) {
        redisClient.set(roleMenuIdsKey(roleId), menuIds);
    }

    public void removeRoleMenuIds(String roleId) {
        redisClient.delete(roleMenuIdsKey(roleId));
    }

    public String currentVersion() {
        String version = redisClient.get(versionKey());
        if (StringUtils.isBlank(version)) {
            version = IdGen.uuid();
            redisClient.set(versionKey(), version);
        }
        return version;
    }

    public void touchVersion() {
        redisClient.set(versionKey(), IdGen.uuid(), VERSION_EXPIRE_SECONDS);
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
}
