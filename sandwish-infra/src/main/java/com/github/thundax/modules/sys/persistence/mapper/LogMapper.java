package com.github.thundax.modules.sys.persistence.mapper;

import com.github.thundax.common.persistence.CrudDao;
import com.github.thundax.common.persistence.annotation.MyBatisDao;
import com.github.thundax.modules.sys.persistence.dataobject.LogDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 日志 MyBatis Mapper。
 */
@MyBatisDao
public interface LogMapper extends CrudDao<LogDO> {

    /**
     * 写入。
     *
     * @param list 列表
     * @return 影响记录数
     */
    int insertList(@Param("list") List<LogDO> list);

    /**
     * 批量删除。
     *
     * @param log 查询条件
     * @return 影响记录数
     */
    int batchDelete(LogDO log);
}
