package com.github.thundax.modules.assist.service.impl;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.SnowflakeIdGenerator;
import com.github.thundax.modules.assist.dao.AsyncTaskDao;
import com.github.thundax.modules.assist.entity.AsyncTask;
import com.github.thundax.modules.assist.service.AsyncTaskService;
import org.springframework.stereotype.Service;

@Service
public class AsyncTaskServiceImpl implements AsyncTaskService {

    private final AsyncTaskDao asyncTaskDao;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public AsyncTaskServiceImpl(AsyncTaskDao asyncTaskDao) {
        this.asyncTaskDao = asyncTaskDao;
    }

    @Override
    public AsyncTask getById(EntityId id) {
        return asyncTaskDao.getById(id);
    }

    @Override
    public EntityId add(AsyncTask asyncTask) {
        if (asyncTask.getId() == null) {
            asyncTask.setId(idGenerator.nextId());
        }
        asyncTaskDao.insert(asyncTask);
        return asyncTask.getId();
    }

    @Override
    public void update(AsyncTask asyncTask) {
        asyncTaskDao.update(asyncTask);
    }

    @Override
    public void deleteById(EntityId id) {
        asyncTaskDao.deleteById(id);
    }
}
