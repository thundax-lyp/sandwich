package com.github.thundax.modules.assist.dao;

import com.github.thundax.common.domain.SortDirection;
import com.github.thundax.modules.assist.entity.AsyncTask;
import com.github.thundax.modules.assist.entity.valueobject.AsyncTaskId;
import java.util.List;

public interface AsyncTaskDao {

    AsyncTask getById(AsyncTaskId id);

    List<AsyncTask> list(SortDirection sortDirection);

    int maxPriority();

    AsyncTaskId insert(AsyncTask asyncTask);

    void update(AsyncTask asyncTask);

    int updatePriority(AsyncTaskId id, int priority);

    void deleteById(AsyncTaskId id);
}
