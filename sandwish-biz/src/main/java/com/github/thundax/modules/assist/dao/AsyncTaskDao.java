package com.github.thundax.modules.assist.dao;

import com.github.thundax.modules.assist.entity.AsyncTask;

/**
 * 异步任务 DAO。
 */
public interface AsyncTaskDao {

    AsyncTask get(String id);

    void save(AsyncTask asyncTask);

    void delete(AsyncTask asyncTask);
}
