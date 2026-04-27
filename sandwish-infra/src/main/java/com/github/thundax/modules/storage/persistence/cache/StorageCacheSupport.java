package com.github.thundax.modules.storage.persistence.cache;

import com.github.thundax.common.Constants;
import com.github.thundax.common.utils.IdGen;
import org.apache.commons.lang3.StringUtils;
import com.github.thundax.common.utils.redis.RedisClient;
import com.github.thundax.modules.storage.entity.Storage;
import org.springframework.stereotype.Component;

/** 存储文件缓存支撑。 */
@Component
public class StorageCacheSupport {

    private static final int OBJECT_EXPIRE_SECONDS = 3600;
    private static final int VERSION_EXPIRE_SECONDS = OBJECT_EXPIRE_SECONDS + 5;
    private static final String CACHE_SECTION = Constants.CACHE_PREFIX + "assist.storage.";
    private static final String ID_PREFIX = "id_";
    private static final String VERSION_KEY = "version";

    private final RedisClient redisClient;

    public StorageCacheSupport(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    public Storage getById(String id) {
        return redisClient.get(objectKey(id), Storage.class);
    }

    public void putById(Storage storage) {
        if (storage != null && StringUtils.isNotBlank(storage.getId())) {
            redisClient.set(objectKey(storage.getId()), storage, OBJECT_EXPIRE_SECONDS);
        }
    }

    public void removeById(String id) {
        redisClient.delete(objectKey(id));
        touchVersion();
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

    private String versionKey() {
        return CACHE_SECTION + VERSION_KEY;
    }
}
