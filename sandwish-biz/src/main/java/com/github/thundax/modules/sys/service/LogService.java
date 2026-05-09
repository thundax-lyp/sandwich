package com.github.thundax.modules.sys.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.service.query.LogQuery;
import java.util.List;

public interface LogService {

    Log getById(EntityId id);

    List<Log> list(LogQuery query);

    PageResult<Log> page(LogQuery query, PageQuery page);

    EntityId add(Log log);

    void update(Log log);

    int deleteById(EntityId id);

    int batchInsert(List<Log> list);

    int batchDelete(LogQuery query);
}
