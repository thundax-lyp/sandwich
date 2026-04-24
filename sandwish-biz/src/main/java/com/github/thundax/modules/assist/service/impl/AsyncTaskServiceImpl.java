package com.github.thundax.modules.assist.service.impl;

import com.github.thundax.common.utils.IdGen;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.modules.assist.dao.AsyncTaskDao;
import com.github.thundax.modules.assist.entity.AsyncTask;
import com.github.thundax.modules.assist.service.AsyncTaskService;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author wdit
 */
@Service
public class AsyncTaskServiceImpl implements AsyncTaskService {

    private final AsyncTaskDao asyncTaskDao;

    public AsyncTaskServiceImpl(AsyncTaskDao asyncTaskDao) {
        this.asyncTaskDao = asyncTaskDao;
    }

    @Override
    public AsyncTask get(String id) {
        return asyncTaskDao.get(id);
    }

    @Override
    public void save(AsyncTask asyncTask) {
        if (StringUtils.isEmpty(asyncTask.getId())) {
            asyncTask.setId(IdGen.uuid());
            asyncTask.setCreateDate(new Date());
        }

        asyncTask.setUpdateDate(new Date());

        asyncTaskDao.save(asyncTask);
    }

    @Override
    public void delete(AsyncTask asyncTask) {
        asyncTaskDao.delete(asyncTask);
    }

}
