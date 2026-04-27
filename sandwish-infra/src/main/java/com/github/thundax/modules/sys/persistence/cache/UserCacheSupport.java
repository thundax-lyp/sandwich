package com.github.thundax.modules.sys.persistence.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.thundax.common.Constants;
import com.github.thundax.common.utils.IdGen;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.common.utils.redis.RedisClient;
import com.github.thundax.modules.sys.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户缓存支撑。
 */
@Component
public class UserCacheSupport {

    private static final int OBJECT_EXPIRE_SECONDS = 3600;
    private static final int VERSION_EXPIRE_SECONDS = OBJECT_EXPIRE_SECONDS + 5;
    private static final String CACHE_SECTION = Constants.CACHE_PREFIX + "sys.user";
    private static final String ID_PREFIX = "id_";
    private static final String VERSION_KEY = "version";
    private static final String ROLES_PREFIX = ".roles_";

    private final RedisClient redisClient;

    public UserCacheSupport(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    public User getById(String id) {
        return redisClient.get(objectKey(id), User.class);
    }

    public void putById(User user) {
        if (user != null && StringUtils.isNotBlank(user.getId())) {
            redisClient.set(objectKey(user.getId()), user, OBJECT_EXPIRE_SECONDS);
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

    public List<String> getUserRoleIds(String userId) {
        return redisClient.get(userRoleIdsKey(userId), new TypeReference<List<String>>() {
        });
    }

    public void putUserRoleIds(String userId, List<String> roleIds) {
        redisClient.set(userRoleIdsKey(userId), roleIds);
    }

    public void removeUserRoleIds(String userId) {
        redisClient.delete(userRoleIdsKey(userId));
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

    private String userRoleIdsKey(String userId) {
        return CACHE_SECTION + ROLES_PREFIX + userId;
    }

    private String versionKey() {
        return CACHE_SECTION + VERSION_KEY;
    }
}
