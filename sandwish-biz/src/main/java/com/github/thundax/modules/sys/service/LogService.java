package com.github.thundax.modules.sys.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.modules.sys.entity.Log;
import java.util.List;

public interface LogService {

    Log get(EntityId id);

    List<Log> findList(Log log);

    Page<Log> findPage(Log log, Page<Log> page);

    void add(Log log);

    void update(Log log);

    int delete(Log log);

    /**
     * 写入
     *
     * @param list 列表
     * @return 影响记录数
     */
    int insertList(List<Log> list);

    /**
     * 批量删除
     *
     * @param log 查询条件
     * @return 影响记录数
     */
    int batchDelete(Log log);
}
