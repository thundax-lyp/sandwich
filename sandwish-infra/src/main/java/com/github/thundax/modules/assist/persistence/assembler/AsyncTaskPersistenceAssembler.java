package com.github.thundax.modules.assist.persistence.assembler;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.assist.entity.AsyncTask;
import com.github.thundax.modules.assist.entity.enums.AsyncTaskStatus;
import com.github.thundax.modules.assist.persistence.dataobject.AsyncTaskDO;

public final class AsyncTaskPersistenceAssembler {

    private AsyncTaskPersistenceAssembler() {}

    public static AsyncTaskDO toDataObject(AsyncTask entity) {
        if (entity == null) {
            return null;
        }
        AsyncTaskDO dataObject = new AsyncTaskDO();
        dataObject.setId(EntityIdCodec.toValue(entity.getId()));
        dataObject.setTitle(entity.getTitle());
        dataObject.setStatus(entity.getStatus().value());
        dataObject.setMessage(entity.getMessage());
        dataObject.setData(entity.getData());
        dataObject.setIsPrivate(entity.getPrivate());
        dataObject.setExpiredSeconds(entity.getExpiredSeconds());
        dataObject.setPriority(priorityOrDefault(entity.getPriority()));
        dataObject.setRemarks(entity.getRemarks());
        dataObject.setCreateDate(entity.getCreateDate());
        dataObject.setCreateBy(entity.getCreateUserId());
        dataObject.setUpdateDate(entity.getUpdateDate());
        dataObject.setUpdateBy(entity.getUpdateUserId());
        return dataObject;
    }

    public static AsyncTask toEntity(AsyncTaskDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        AsyncTask entity = new AsyncTask();
        entity.setId(EntityIdCodec.toDomain(dataObject.getId()));
        entity.setTitle(dataObject.getTitle());
        entity.setStatus(AsyncTaskStatus.from(dataObject.getStatus()));
        entity.setMessage(dataObject.getMessage());
        entity.setData(dataObject.getData());
        entity.setPrivate(dataObject.getIsPrivate());
        entity.setExpiredSeconds(dataObject.getExpiredSeconds());
        entity.setPriority(priorityOrDefault(dataObject.getPriority()));
        entity.setRemarks(dataObject.getRemarks());
        entity.setCreateDate(dataObject.getCreateDate());
        entity.setCreateUserId(dataObject.getCreateBy());
        entity.setUpdateDate(dataObject.getUpdateDate());
        entity.setUpdateUserId(dataObject.getUpdateBy());
        return entity;
    }

    private static int priorityOrDefault(Integer priority) {
        return priority == null || priority < 0 ? 0 : priority;
    }
}
