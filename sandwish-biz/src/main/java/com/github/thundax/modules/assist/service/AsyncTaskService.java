package com.github.thundax.modules.assist.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.assist.entity.AsyncTask;
import com.github.thundax.modules.assist.service.command.AsyncTaskCommand;
import com.github.thundax.modules.assist.service.query.AsyncTaskQuery;

public interface AsyncTaskService {

    AsyncTask get(AsyncTaskQuery query);

    EntityId create(AsyncTaskCommand command);

    void change(AsyncTaskCommand command);

    void remove(AsyncTaskCommand command);
}
