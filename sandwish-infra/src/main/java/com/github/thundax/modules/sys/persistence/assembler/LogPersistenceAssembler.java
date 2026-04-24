package com.github.thundax.modules.sys.persistence.assembler;

import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.persistence.dataobject.LogDO;

import java.util.List;

/**
 * 日志业务模型与持久化对象转换器。
 */
public final class LogPersistenceAssembler {

    private LogPersistenceAssembler() {
    }

    public static LogDO toDataObject(Log entity) {
        if (entity == null) {
            return null;
        }
        LogDO dataObject = new LogDO();
        dataObject.setId(entity.getId());
        dataObject.setIsNewRecord(entity.getIsNewRecord());
        dataObject.setUserId(entity.getUserId());
        dataObject.setType(entity.getType());
        dataObject.setLogDate(entity.getLogDate());
        dataObject.setTitle(entity.getTitle());
        dataObject.setRemoteAddr(entity.getRemoteAddr());
        dataObject.setUserAgent(entity.getUserAgent());
        dataObject.setMethod(entity.getMethod());
        dataObject.setRequestUri(entity.getRequestUri());
        dataObject.setRequestParams(entity.getRequestParams());
        dataObject.setPriority(entity.getPriority());
        dataObject.setRemarks(entity.getRemarks());
        dataObject.setCreateDate(entity.getCreateDate());
        dataObject.setCreateUserId(entity.getCreateUserId());
        dataObject.setUpdateDate(entity.getUpdateDate());
        dataObject.setUpdateUserId(entity.getUpdateUserId());
        dataObject.setDelFlag(entity.getDelFlag());
        dataObject.setQuery(toDataObjectQuery(entity.getQuery()));
        return dataObject;
    }

    public static Log toEntity(LogDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        Log entity = new Log();
        entity.setId(dataObject.getId());
        entity.setIsNewRecord(dataObject.getIsNewRecord());
        entity.setUserId(dataObject.getUserId());
        entity.setType(dataObject.getType());
        entity.setLogDate(dataObject.getLogDate());
        entity.setTitle(dataObject.getTitle());
        entity.setRemoteAddr(dataObject.getRemoteAddr());
        entity.setUserAgent(dataObject.getUserAgent());
        entity.setMethod(dataObject.getMethod());
        entity.setRequestUri(dataObject.getRequestUri());
        entity.setRequestParams(dataObject.getRequestParams());
        entity.setPriority(dataObject.getPriority());
        entity.setRemarks(dataObject.getRemarks());
        entity.setCreateDate(dataObject.getCreateDate());
        entity.setCreateUserId(dataObject.getCreateUserId());
        entity.setUpdateDate(dataObject.getUpdateDate());
        entity.setUpdateUserId(dataObject.getUpdateUserId());
        entity.setDelFlag(dataObject.getDelFlag());
        entity.setQuery(toEntityQuery(dataObject.getQuery()));
        return entity;
    }

    public static List<Log> toEntityList(List<LogDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<Log> entities = ListUtils.newArrayList();
        for (LogDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }

    public static List<LogDO> toDataObjectList(List<Log> entities) {
        if (entities == null) {
            return null;
        }
        List<LogDO> dataObjects = ListUtils.newArrayList();
        for (Log entity : entities) {
            dataObjects.add(toDataObject(entity));
        }
        return dataObjects;
    }

    private static LogDO.Query toDataObjectQuery(Log.Query query) {
        if (query == null) {
            return null;
        }
        LogDO.Query dataObjectQuery = new LogDO.Query();
        dataObjectQuery.setType(query.getType());
        dataObjectQuery.setRemoteAddr(query.getRemoteAddr());
        dataObjectQuery.setTitle(query.getTitle());
        dataObjectQuery.setRequestUri(query.getRequestUri());
        dataObjectQuery.setUserLoginName(query.getUserLoginName());
        dataObjectQuery.setUserName(query.getUserName());
        dataObjectQuery.setBeginDate(query.getBeginDate());
        dataObjectQuery.setEndDate(query.getEndDate());
        return dataObjectQuery;
    }

    private static Log.Query toEntityQuery(LogDO.Query query) {
        if (query == null) {
            return null;
        }
        Log.Query entityQuery = new Log.Query();
        entityQuery.setType(query.getType());
        entityQuery.setRemoteAddr(query.getRemoteAddr());
        entityQuery.setTitle(query.getTitle());
        entityQuery.setRequestUri(query.getRequestUri());
        entityQuery.setUserLoginName(query.getUserLoginName());
        entityQuery.setUserName(query.getUserName());
        entityQuery.setBeginDate(query.getBeginDate());
        entityQuery.setEndDate(query.getEndDate());
        return entityQuery;
    }
}
