package com.github.thundax.modules.sys.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.service.query.LogQuery;
import java.util.List;

public interface LogService {

    Log getById(EntityId id);

    List<Log> list(LogQuery query);

    Page<Log> page(LogQuery query, Page<Log> page);

    void add(Log log);

    void update(Log log);

    int deleteById(EntityId id);

    int batchInsert(List<Log> list);

    int batchDelete(LogQuery query);
}
