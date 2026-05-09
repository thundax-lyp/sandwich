package com.github.thundax.modules.sys.persistence.dao;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.github.thundax.common.Constants;
import com.github.thundax.modules.sys.dao.SmsValidateCodeDao;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Repository;

@Repository
public class SmsValidateCodeDaoImpl implements SmsValidateCodeDao {

    private static final String CACHE_MOBILE = Constants.CACHE_PREFIX + "smsValidateMobile.";

    @CreateCache(name = CACHE_MOBILE, cacheType = CacheType.REMOTE)
    private Cache<String, String> cache;

    @Override
    public boolean canSend(String mobile) {
        return cache.get(mobile) == null;
    }

    @Override
    public void markSent(String mobile, int expiredSeconds) {
        cache.put(mobile, "1", expiredSeconds, TimeUnit.SECONDS);
    }
}
