package com.github.thundax.modules.assist.persistence.dao;

import com.github.thundax.common.Constants;
import com.github.thundax.common.utils.redis.RedisClient;
import com.github.thundax.modules.assist.dao.AsyncTaskDao;
import com.github.thundax.modules.assist.entity.AsyncTask;
import com.github.thundax.modules.assist.persistence.assembler.AsyncTaskPersistenceAssembler;
import com.github.thundax.modules.assist.persistence.dataobject.AsyncTaskDO;
import org.springframework.stereotype.Repository;

/**
 * 异步任务 Redis DAO 实现。
 */
@Repository
public class AsyncTaskDaoImpl implements AsyncTaskDao {

    private static final String CACHE_SECTION = Constants.CACHE_PREFIX + "assist.asyncTask.";

    private final RedisClient redisClient;

    public AsyncTaskDaoImpl(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    @Override
    public AsyncTask get(String id) {
        return AsyncTaskPersistenceAssembler.toEntity(redisClient.get(cacheKey(id), AsyncTaskDO.class));
    }

    @Override
    public void save(AsyncTask asyncTask) {
        redisClient.set(cacheKey(asyncTask.getId()),
                AsyncTaskPersistenceAssembler.toDataObject(asyncTask),
                asyncTask.getExpiredSeconds());
    }

    @Override
    public void delete(AsyncTask asyncTask) {
        redisClient.delete(cacheKey(asyncTask.getId()));
    }

    private String cacheKey(String id) {
        return CACHE_SECTION + id;
    }
}
