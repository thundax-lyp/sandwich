package com.github.thundax.modules.sys.persistence.assembler;

import com.github.thundax.modules.sys.entity.UploadFile;
import com.github.thundax.modules.sys.persistence.dataobject.UploadFileDO;
import java.util.ArrayList;
import java.util.List;

public final class UploadFilePersistenceAssembler {

    private UploadFilePersistenceAssembler() {}

    public static UploadFileDO toDataObject(UploadFile entity) {
        if (entity == null) {
            return null;
        }
        UploadFileDO dataObject = new UploadFileDO();
        dataObject.setId(entity.getId());
        dataObject.setName(entity.getName());
        dataObject.setExtendName(entity.getExtendName());
        dataObject.setMimeType(entity.getMimeType());
        dataObject.setSize(entity.getSize());
        dataObject.setPath(entity.getPath());
        dataObject.setCreateDate(entity.getCreateDate());
        return dataObject;
    }

    public static UploadFile toEntity(UploadFileDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        UploadFile entity = new UploadFile();
        entity.setId(dataObject.getId());
        entity.setName(dataObject.getName());
        entity.setExtendName(dataObject.getExtendName());
        entity.setMimeType(dataObject.getMimeType());
        entity.setSize(dataObject.getSize());
        entity.setPath(dataObject.getPath());
        entity.setCreateDate(dataObject.getCreateDate());
        return entity;
    }

    public static List<UploadFile> toEntityList(List<UploadFileDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<UploadFile> entities = new ArrayList<>();
        for (UploadFileDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }
}
