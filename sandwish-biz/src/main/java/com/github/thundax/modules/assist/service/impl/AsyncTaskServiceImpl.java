package com.github.thundax.modules.assist.service.impl;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.id.UuidHelper;
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
    public AsyncTask getById(EntityId id) {
        return asyncTaskDao.getById(id);
    }

    @Override
    public void add(AsyncTask asyncTask) {
        if (StringUtils.isBlank(EntityIdCodec.toValue(asyncTask.getId()))) {
            asyncTask.setId(EntityIdCodec.toDomain(UuidHelper.compact()));
        }
        asyncTaskDao.insert(asyncTask);
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
