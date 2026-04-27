package com.github.thundax.modules.sys.persistence.cache;

import com.github.thundax.common.Constants;
import com.github.thundax.common.utils.IdGen;
import org.apache.commons.lang3.StringUtils;
import com.github.thundax.common.utils.redis.RedisClient;
import com.github.thundax.modules.sys.entity.Menu;
import org.springframework.stereotype.Component;

/** 菜单缓存支撑。 */
@Component
public class MenuCacheSupport {

    private static final int OBJECT_EXPIRE_SECONDS = 3600;
    private static final int VERSION_EXPIRE_SECONDS = OBJECT_EXPIRE_SECONDS + 5;
    private static final String CACHE_SECTION = Constants.CACHE_PREFIX + "sys.menu.";
    private static final String ID_PREFIX = "id_";
    private static final String VERSION_KEY = "version";

    private final RedisClient redisClient;

    public MenuCacheSupport(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    public Menu getById(String id) {
        return redisClient.get(objectKey(id), Menu.class);
    }

    public void putById(Menu menu) {
        if (menu != null && StringUtils.isNotBlank(menu.getId())) {
            redisClient.set(objectKey(menu.getId()), menu, OBJECT_EXPIRE_SECONDS);
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
