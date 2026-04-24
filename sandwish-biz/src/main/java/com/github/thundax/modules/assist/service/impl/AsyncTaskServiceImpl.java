package com.github.thundax.modules.assist.service.impl;

import com.github.thundax.common.Constants;
import com.github.thundax.common.utils.IdGen;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.common.utils.redis.RedisClient;
import com.github.thundax.modules.assist.entity.AsyncTask;
import com.github.thundax.modules.assist.service.AsyncTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author wdit
 */
@Service
public class AsyncTaskServiceImpl implements AsyncTaskService {

    private final RedisClient redisClient;

    @Autowired
    public AsyncTaskServiceImpl(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    protected String getCacheSection() {
        return Constants.CACHE_PREFIX + "assist.asyncTask.";
    }

    @Override
    public AsyncTask get(String id) {
        return redisClient.get(getCacheSection() + id, AsyncTask.class);
    }

    @Override
    public void save(AsyncTask asyncTask) {
        if (StringUtils.isEmpty(asyncTask.getId())) {
            asyncTask.setId(IdGen.uuid());
            asyncTask.setCreateDate(new Date());
        }

        asyncTask.setUpdateDate(new Date());

        redisClient.set(getCacheSection() + asyncTask.getId(), asyncTask, asyncTask.getExpiredSeconds());
    }

    @Override
    public void delete(AsyncTask asyncTask) {
        redisClient.delete(getCacheSection() + asyncTask.getId());
    }

}
