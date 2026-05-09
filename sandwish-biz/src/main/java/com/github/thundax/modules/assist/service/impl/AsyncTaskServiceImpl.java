package com.github.thundax.modules.assist.service.impl;

import com.github.thundax.modules.assist.dao.AsyncTaskDao;
import com.github.thundax.modules.assist.entity.AsyncTask;
import com.github.thundax.modules.assist.entity.valueobject.AsyncTaskId;
import com.github.thundax.modules.assist.service.AsyncTaskService;
import com.github.thundax.modules.assist.service.command.AsyncTaskCommand;
import com.github.thundax.modules.audit.annotation.AuditLog;
import com.github.thundax.modules.audit.entity.enums.AuditAction;
import org.springframework.stereotype.Service;

@Service
public class AsyncTaskServiceImpl implements AsyncTaskService {

    private final AsyncTaskDao asyncTaskDao;

    public AsyncTaskServiceImpl(AsyncTaskDao asyncTaskDao) {
        this.asyncTaskDao = asyncTaskDao;
    }

    @Override
    public AsyncTask get(AsyncTaskId id) {
        if (id == null) {
            return null;
        }
        return asyncTaskDao.getById(id);
    }

    @Override
    @AuditLog(type = "AsyncTask", id = "", action = AuditAction.CREATE, summary = "创建异步任务", recordWhenUnchanged = true)
    public AsyncTaskId create(AsyncTaskCommand command) {
        AsyncTask asyncTask = command.getAsyncTask();
        return asyncTaskDao.insert(asyncTask);
    }

    @Override
    @AuditLog(type = "AsyncTask", id = "#command.asyncTask.id.value()", action = AuditAction.UPDATE, summary = "更新异步任务")
    public void change(AsyncTaskCommand command) {
        asyncTaskDao.update(command.getAsyncTask());
    }

    @Override
    @AuditLog(
            type = "AsyncTask",
            id = "#id.value()",
            action = AuditAction.DELETE,
            summary = "删除异步任务",
            recordWhenUnchanged = true)
    public void remove(AsyncTaskId id) {
        asyncTaskDao.deleteById(id);
    }
}
