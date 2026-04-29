package com.github.thundax.modules.assist.service.impl;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.utils.IdGen;
import com.github.thundax.modules.assist.dao.AsyncTaskDao;
import com.github.thundax.modules.assist.entity.AsyncTask;
import com.github.thundax.modules.assist.service.AsyncTaskService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class AsyncTaskServiceImpl implements AsyncTaskService {

    private final AsyncTaskDao asyncTaskDao;

    public AsyncTaskServiceImpl(AsyncTaskDao asyncTaskDao) {
        this.asyncTaskDao = asyncTaskDao;
    }

    @Override
    public AsyncTask get(EntityId id) {
        return asyncTaskDao.get(id);
    }

    @Override
    public void add(AsyncTask asyncTask) {
        if (StringUtils.isBlank(asyncTask.getId())) {
            asyncTask.setId(IdGen.uuid());
        }
        asyncTaskDao.insert(asyncTask);
    }

    @Override
    public void update(AsyncTask asyncTask) {
        asyncTaskDao.update(asyncTask);
    }

    @Override
    public void delete(AsyncTask asyncTask) {
        asyncTaskDao.delete(asyncTask);
    }
}
