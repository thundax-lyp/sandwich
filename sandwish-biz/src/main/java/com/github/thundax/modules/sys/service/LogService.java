package com.github.thundax.modules.sys.service;

import com.github.thundax.common.service.CrudService;
import com.github.thundax.modules.sys.entity.Log;

import java.util.List;

/**
 * @author wdit
 */
public interface LogService extends CrudService<Log> {

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
