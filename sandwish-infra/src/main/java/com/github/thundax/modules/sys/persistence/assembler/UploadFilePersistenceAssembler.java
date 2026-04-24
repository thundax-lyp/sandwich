package com.github.thundax.modules.sys.persistence.assembler;

import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.modules.sys.entity.UploadFile;
import com.github.thundax.modules.sys.persistence.dataobject.UploadFileDO;

import java.util.List;

/**
 * 上传文件业务模型与持久化对象转换器。
 */
public final class UploadFilePersistenceAssembler {

    private UploadFilePersistenceAssembler() {
    }

    public static UploadFileDO toDataObject(UploadFile entity) {
        if (entity == null) {
            return null;
        }
        UploadFileDO dataObject = new UploadFileDO();
        dataObject.setId(entity.getId());
        dataObject.setIsNewRecord(entity.getIsNewRecord());
        dataObject.setName(entity.getName());
        dataObject.setExtendName(entity.getExtendName());
        dataObject.setMimeType(entity.getMimeType());
        dataObject.setSize(entity.getSize());
        dataObject.setPath(entity.getPath());
        dataObject.setContent(entity.getContent());
        dataObject.setPriority(entity.getPriority());
        dataObject.setRemarks(entity.getRemarks());
        dataObject.setCreateDate(entity.getCreateDate());
        dataObject.setCreateUserId(entity.getCreateUserId());
        dataObject.setUpdateDate(entity.getUpdateDate());
        dataObject.setUpdateUserId(entity.getUpdateUserId());
        dataObject.setDelFlag(entity.getDelFlag());
        return dataObject;
    }

    public static UploadFile toEntity(UploadFileDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        UploadFile entity = new UploadFile();
        entity.setId(dataObject.getId());
        entity.setIsNewRecord(dataObject.getIsNewRecord());
        entity.setName(dataObject.getName());
        entity.setExtendName(dataObject.getExtendName());
        entity.setMimeType(dataObject.getMimeType());
        entity.setSize(dataObject.getSize());
        entity.setPath(dataObject.getPath());
        entity.setContent(dataObject.getContent());
        entity.setPriority(dataObject.getPriority());
        entity.setRemarks(dataObject.getRemarks());
        entity.setCreateDate(dataObject.getCreateDate());
        entity.setCreateUserId(dataObject.getCreateUserId());
        entity.setUpdateDate(dataObject.getUpdateDate());
        entity.setUpdateUserId(dataObject.getUpdateUserId());
        entity.setDelFlag(dataObject.getDelFlag());
        return entity;
    }

    public static List<UploadFile> toEntityList(List<UploadFileDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<UploadFile> entities = ListUtils.newArrayList();
        for (UploadFileDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }
}
