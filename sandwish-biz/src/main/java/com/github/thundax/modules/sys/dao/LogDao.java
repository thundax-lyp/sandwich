package com.github.thundax.modules.sys.dao;

import com.github.thundax.common.persistence.CrudDao;
import com.github.thundax.modules.sys.entity.Log;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wdit
 */
public interface LogDao extends CrudDao<Log> {

    /**
     * 写入
     *
     * @param list 列表
     * @return 影响记录数
     */
    int insertList(@Param("list") List<Log> list);

    /**
     * 批量删除
     *
     * @param log 查询条件
     * @return 影响记录数
     */
    int batchDelete(Log log);

}
