package com.github.thundax.modules.assist.persistence.dao;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.github.thundax.common.Constants;
import com.github.thundax.modules.assist.dao.KeypairPrivateKeyDao;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Repository;

@Repository
public class KeypairPrivateKeyDaoImpl implements KeypairPrivateKeyDao {

    @CreateCache(name = Constants.CACHE_PRIVATE_KEY, cacheType = CacheType.REMOTE)
    private Cache<String, String> cache;

    @Override
    public void save(String token, String privateKey, int expiredSeconds) {
        cache.put(token, privateKey, expiredSeconds, TimeUnit.SECONDS);
    }

    @Override
    public String getByToken(String token) {
        return cache.get(token);
    }
}
