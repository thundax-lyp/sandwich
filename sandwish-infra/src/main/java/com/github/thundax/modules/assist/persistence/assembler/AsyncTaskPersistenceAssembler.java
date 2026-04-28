package com.github.thundax.modules.assist.persistence.assembler;

import com.github.thundax.modules.assist.entity.AsyncTask;
import com.github.thundax.modules.assist.persistence.dataobject.AsyncTaskDO;

/** 异步任务业务模型与持久化对象转换器。 */
public final class AsyncTaskPersistenceAssembler {

    private AsyncTaskPersistenceAssembler() {}

    public static AsyncTaskDO toDataObject(AsyncTask entity) {
        if (entity == null) {
            return null;
        }
        AsyncTaskDO dataObject = new AsyncTaskDO();
        dataObject.setId(entity.getId());
        dataObject.setNewRecord(entity.getIsNewRecord());
        dataObject.setTitle(entity.getTitle());
        dataObject.setStatus(entity.getStatus());
        dataObject.setMessage(entity.getMessage());
        dataObject.setData(entity.getData());
        dataObject.setIsPrivate(entity.getPrivate());
        dataObject.setExpiredSeconds(entity.getExpiredSeconds());
        dataObject.setPriority(entity.getPriority());
        dataObject.setRemarks(entity.getRemarks());
        dataObject.setCreateDate(entity.getCreateDate());
        dataObject.setCreateUserId(entity.getCreateUserId());
        dataObject.setUpdateDate(entity.getUpdateDate());
        dataObject.setUpdateUserId(entity.getUpdateUserId());
        dataObject.setDelFlag(entity.getDelFlag());
        return dataObject;
    }

    public static AsyncTask toEntity(AsyncTaskDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        AsyncTask entity = new AsyncTask();
        entity.setId(dataObject.getId());
        entity.setIsNewRecord(dataObject.isNewRecord());
        entity.setTitle(dataObject.getTitle());
        entity.setStatus(dataObject.getStatus());
        entity.setMessage(dataObject.getMessage());
        entity.setData(dataObject.getData());
        entity.setPrivate(dataObject.getIsPrivate());
        entity.setExpiredSeconds(dataObject.getExpiredSeconds());
        entity.setPriority(dataObject.getPriority());
        entity.setRemarks(dataObject.getRemarks());
        entity.setCreateDate(dataObject.getCreateDate());
        entity.setCreateUserId(dataObject.getCreateUserId());
        entity.setUpdateDate(dataObject.getUpdateDate());
        entity.setUpdateUserId(dataObject.getUpdateUserId());
        entity.setDelFlag(dataObject.getDelFlag());
        return entity;
    }
}
