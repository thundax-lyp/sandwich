package com.github.thundax.modules.sys.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.entity.Log;
import java.util.Date;
import java.util.List;

public interface LogDao {

    Log getById(EntityId id);

    List<Log> listByIds(List<String> idList);

    List<Log> list(
            String type,
            String remoteAddr,
            String userLoginName,
            String userName,
            String title,
            String requestUri,
            Date beginDate,
            Date endDate);

    Page<Log> page(
            String type,
            String remoteAddr,
            String userLoginName,
            String userName,
            String title,
            String requestUri,
            Date beginDate,
            Date endDate,
            int pageNo,
            int pageSize);

    EntityId insert(Log log);

    int update(Log log);

    int deleteById(EntityId id);

    List<EntityId> batchInsert(List<Log> list);

    int batchDelete(String type, String remoteAddr, String title, String requestUri, Date beginDate, Date endDate);
}
