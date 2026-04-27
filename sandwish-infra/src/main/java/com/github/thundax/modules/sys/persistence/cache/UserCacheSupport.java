package com.github.thundax.modules.sys.persistence.cache;

import com.github.thundax.common.Constants;
import com.github.thundax.common.utils.IdGen;
import com.github.thundax.common.utils.redis.RedisClient;
import org.springframework.stereotype.Component;

/**
 * 用户缓存支撑。
 */
@Component
public class UserCacheSupport {

    private static final int OBJECT_EXPIRE_SECONDS = 3600;
    private static final int VERSION_EXPIRE_SECONDS = OBJECT_EXPIRE_SECONDS + 5;
    private static final String CACHE_SECTION = Constants.CACHE_PREFIX + "sys.user";
    private static final String VERSION_KEY = "version";

    private final RedisClient redisClient;

    public UserCacheSupport(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    public void removeAll() {
        redisClient.deleteByPattern(CACHE_SECTION + "*");
        touchVersion();
    }

    public void touchVersion() {
        redisClient.set(versionKey(), IdGen.uuid(), VERSION_EXPIRE_SECONDS);
    }

    private String versionKey() {
        return CACHE_SECTION + VERSION_KEY;
    }
}
