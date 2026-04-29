package com.github.thundax.modules.assist.dao;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.assist.entity.AsyncTask;

public interface AsyncTaskDao {

    AsyncTask get(EntityId id);

    void insert(AsyncTask asyncTask);

    void update(AsyncTask asyncTask);

    void delete(AsyncTask asyncTask);
}
