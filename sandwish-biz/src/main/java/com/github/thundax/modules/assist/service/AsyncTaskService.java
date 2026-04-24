package com.github.thundax.modules.assist.service;

import com.github.thundax.modules.assist.entity.AsyncTask;

/**
 * @author wdit
 */
public interface AsyncTaskService {

    /**
     * 获取
     *
     * @param id id
     * @return 对象
     */
    AsyncTask get(String id);

    /**
     * 保存
     *
     * @param asyncTask asyncTask
     */
    void save(AsyncTask asyncTask);

    /**
     * 删除
     *
     * @param asyncTask asyncTask
     */
    void delete(AsyncTask asyncTask);

}
