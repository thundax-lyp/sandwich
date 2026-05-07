package com.github.thundax.modules.assist.service.impl;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.assist.dao.AsyncTaskDao;
import com.github.thundax.modules.assist.entity.AsyncTask;
import com.github.thundax.modules.assist.service.AsyncTaskService;
import org.springframework.stereotype.Service;

@Service
public class AsyncTaskServiceImpl implements AsyncTaskService {

    private final AsyncTaskDao asyncTaskDao;

    public AsyncTaskServiceImpl(AsyncTaskDao asyncTaskDao) {
        this.asyncTaskDao = asyncTaskDao;
    }

    @Override
    public AsyncTask getById(EntityId id) {
        return asyncTaskDao.getById(id);
    }

    @Override
    public EntityId add(AsyncTask asyncTask) {
        return asyncTaskDao.insert(asyncTask);
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
