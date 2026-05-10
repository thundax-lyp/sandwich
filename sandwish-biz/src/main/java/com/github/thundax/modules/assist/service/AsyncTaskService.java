package com.github.thundax.modules.assist.service;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.modules.assist.entity.AsyncTask;
import com.github.thundax.modules.assist.entity.valueobject.AsyncTaskId;
import com.github.thundax.modules.assist.service.command.AsyncTaskCommand;
import com.github.thundax.modules.assist.service.command.AsyncTaskSortCommand;

public interface AsyncTaskService {

    AsyncTask get(AsyncTaskId id);

    AsyncTaskId create(AsyncTaskCommand command);

    void change(AsyncTaskCommand command);

    void remove(AsyncTaskId id);

    void sort(AsyncTaskSortCommand command) throws ApiException;
}
