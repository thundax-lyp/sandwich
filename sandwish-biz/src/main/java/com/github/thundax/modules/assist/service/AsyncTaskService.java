package com.github.thundax.modules.assist.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.assist.entity.AsyncTask;

public interface AsyncTaskService {

    /**
     * 获取
     *
     * @param id id
     * @return 对象
     */
    AsyncTask get(EntityId id);

    /**
     * 新增
     *
     * @param asyncTask asyncTask
     */
    void add(AsyncTask asyncTask);

    /**
     * 更新
     *
     * @param asyncTask asyncTask
     */
    void update(AsyncTask asyncTask);

    /**
     * 删除
     *
     * @param asyncTask asyncTask
     */
    void delete(AsyncTask asyncTask);
}
