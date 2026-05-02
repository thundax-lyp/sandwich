package com.github.thundax.modules.assist.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.assist.entity.AsyncTask;

public interface AsyncTaskService {

    AsyncTask getById(EntityId id);

    void add(AsyncTask asyncTask);

    void update(AsyncTask asyncTask);

    void deleteById(EntityId id);
}
