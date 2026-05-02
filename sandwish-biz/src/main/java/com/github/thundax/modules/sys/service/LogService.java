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

    /**
     * 写入
     *
     * @param list 列表
     * @return 影响记录数
     */
    int batchInsert(List<Log> list);

    /**
     * 批量删除
     *
     * @param log 查询条件
     * @return 影响记录数
     */
    int batchDelete(LogQuery query);
}
