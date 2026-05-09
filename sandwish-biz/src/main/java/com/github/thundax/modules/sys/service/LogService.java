package com.github.thundax.modules.sys.service;

import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.entity.valueobject.LogId;
import com.github.thundax.modules.sys.service.command.CreateLogCommand;
import com.github.thundax.modules.sys.service.query.LogQuery;
import java.util.List;

public interface LogService {

    Log get(LogId id);

    List<Log> list(LogQuery query);

    PageResult<Log> page(LogQuery query, PageQuery page);

    LogId create(CreateLogCommand command);

    int deleteByCondition(LogQuery query);
}
