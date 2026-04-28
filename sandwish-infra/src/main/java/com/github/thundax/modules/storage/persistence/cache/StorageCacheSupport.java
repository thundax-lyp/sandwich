package com.github.thundax.modules.storage.persistence.cache;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.github.thundax.common.Constants;
import com.github.thundax.common.utils.IdGen;
import com.github.thundax.modules.storage.entity.Storage;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;


@Component
public class StorageCacheSupport {

    private static final int OBJECT_EXPIRE_SECONDS = 3600;
    private static final int VERSION_EXPIRE_SECONDS = OBJECT_EXPIRE_SECONDS + 5;
    private static final String CACHE_SECTION = Constants.CACHE_PREFIX + "assist.storage.";
    private static final String ID_PREFIX = "id_";
    private static final String VERSION_KEY = "version";

    @CreateCache(
            name = CACHE_SECTION,
            cacheType = CacheType.REMOTE,
            expire = OBJECT_EXPIRE_SECONDS,
            timeUnit = TimeUnit.SECONDS)
    private Cache<String, Object> cache;

    public Storage getById(String id) {
        return (Storage) cache.get(objectKey(id));
    }

    public void putById(Storage storage) {
        if (storage != null && StringUtils.isNotBlank(storage.getId())) {
            cache.put(objectKey(storage.getId()), storage, OBJECT_EXPIRE_SECONDS, TimeUnit.SECONDS);
        }
    }

    public void removeById(String id) {
        cache.remove(objectKey(id));
        touchVersion();
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

    private String versionKey() {
        return CACHE_SECTION + VERSION_KEY;
    }
}
