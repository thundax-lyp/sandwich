package com.github.thundax.modules.assist.service.impl;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.assist.dao.AsyncTaskDao;
import com.github.thundax.modules.assist.entity.AsyncTask;
import com.github.thundax.modules.assist.service.AsyncTaskService;
import com.github.thundax.modules.assist.service.command.AsyncTaskCommand;
import com.github.thundax.modules.assist.service.query.AsyncTaskQuery;
import org.springframework.stereotype.Service;

@Service
public class AsyncTaskServiceImpl implements AsyncTaskService {

    private final AsyncTaskDao asyncTaskDao;

    public AsyncTaskServiceImpl(AsyncTaskDao asyncTaskDao) {
        this.asyncTaskDao = asyncTaskDao;
    }

    @Override
    public AsyncTask get(AsyncTaskQuery query) {
        if (query == null || query.getId() == null) {
            return null;
        }
        return asyncTaskDao.getById(query.getId());
    }

    @Override
    public EntityId create(AsyncTaskCommand command) {
        AsyncTask asyncTask = command.getAsyncTask();
        return asyncTaskDao.insert(asyncTask);
    }

    @Override
    public void change(AsyncTaskCommand command) {
        asyncTaskDao.update(command.getAsyncTask());
    }

    @Override
    public void remove(AsyncTaskCommand command) {
        asyncTaskDao.deleteById(command.getId());
    }
}
