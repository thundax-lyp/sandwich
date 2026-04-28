package com.github.thundax.modules.sys.dao;

import com.github.thundax.common.persistence.Page;
import com.github.thundax.modules.sys.entity.Log;
import java.util.Date;
import java.util.List;

/** @author wdit */
public interface LogDao {

    Log get(String id);

    List<Log> getMany(List<String> idList);

    List<Log> findList(
            String type,
            String remoteAddr,
            String userLoginName,
            String userName,
            String title,
            String requestUri,
            Date beginDate,
            Date endDate);

    Page<Log> findPage(
            String type,
            String remoteAddr,
            String userLoginName,
            String userName,
            String title,
            String requestUri,
            Date beginDate,
            Date endDate,
            Page<Log> page);

    int insert(Log log);

    int delete(String id);

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
    int batchDelete(
            String type,
            String remoteAddr,
            String title,
            String requestUri,
            Date beginDate,
            Date endDate);
}
