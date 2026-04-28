package com.github.thundax.modules.assist.persistence.dao;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.github.thundax.common.Constants;
import com.github.thundax.modules.assist.dao.AsyncTaskDao;
import com.github.thundax.modules.assist.entity.AsyncTask;
import com.github.thundax.modules.assist.persistence.assembler.AsyncTaskPersistenceAssembler;
import com.github.thundax.modules.assist.persistence.dataobject.AsyncTaskDO;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Repository;


@Repository
public class AsyncTaskDaoImpl implements AsyncTaskDao {

    private static final String CACHE_SECTION = Constants.CACHE_PREFIX + "assist.asyncTask.";

    @CreateCache(name = CACHE_SECTION, cacheType = CacheType.REMOTE)
    private Cache<String, AsyncTaskDO> cache;

    @Override
    public AsyncTask get(String id) {
        return AsyncTaskPersistenceAssembler.toEntity(cache.get(cacheKey(id)));
    }

    @Override
    public void insert(AsyncTask asyncTask) {
        put(asyncTask);
    }

    @Override
    public void update(AsyncTask asyncTask) {
        put(asyncTask);
    }

    @Override
    public void delete(AsyncTask asyncTask) {
        cache.remove(cacheKey(asyncTask.getId()));
    }

    private String cacheKey(String id) {
        return CACHE_SECTION + id;
    }

    private void put(AsyncTask asyncTask) {
        cache.put(
                cacheKey(asyncTask.getId()),
                AsyncTaskPersistenceAssembler.toDataObject(asyncTask),
                asyncTask.getExpiredSeconds(),
                TimeUnit.SECONDS);
    }
}
