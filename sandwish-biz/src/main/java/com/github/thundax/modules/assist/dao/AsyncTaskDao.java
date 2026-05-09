package com.github.thundax.modules.assist.dao;

import com.github.thundax.modules.assist.entity.AsyncTask;
import com.github.thundax.modules.assist.entity.valueobject.AsyncTaskId;

public interface AsyncTaskDao {

    AsyncTask getById(AsyncTaskId id);

    AsyncTaskId insert(AsyncTask asyncTask);

    void update(AsyncTask asyncTask);

    void deleteById(AsyncTaskId id);
}
