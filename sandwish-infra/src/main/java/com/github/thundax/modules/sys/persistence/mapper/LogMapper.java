package com.github.thundax.modules.sys.persistence.mapper;

import com.github.thundax.common.persistence.annotation.MyBatisDao;
import com.github.thundax.modules.sys.persistence.dataobject.LogDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 日志 MyBatis Mapper。
 */
@MyBatisDao
public interface LogMapper {

    LogDO get(LogDO log);

    List<LogDO> getMany(@Param("idList") List<String> idList);

    List<LogDO> findList(LogDO log);

    int insert(LogDO log);

    int update(LogDO log);

    int updatePriority(LogDO log);

    int updateStatus(LogDO log);

    int updateDelFlag(LogDO log);

    int delete(LogDO log);

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
