package com.github.thundax.modules.submission.persistence.assembler;

import com.github.thundax.modules.storage.entity.valueobject.StoredObjectIdCodec;
import com.github.thundax.modules.submission.entity.SubmissionImage;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionIdCodec;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionImageIdCodec;
import com.github.thundax.modules.submission.persistence.dataobject.SubmissionImageDO;
import java.util.ArrayList;
import java.util.List;

public final class SubmissionImagePersistenceAssembler {

    private SubmissionImagePersistenceAssembler() {}

    public static SubmissionImageDO toDataObject(SubmissionImage entity) {
        if (entity == null) {
            return null;
        }
        SubmissionImageDO dataObject = new SubmissionImageDO();
        dataObject.setId(SubmissionImageIdCodec.toValue(entity.getId()));
        dataObject.setSubmissionId(SubmissionIdCodec.toValue(entity.getSubmissionId()));
        dataObject.setStorageObjectId(StoredObjectIdCodec.toValue(entity.getStorageObjectId()));
        dataObject.setSortOrder(sortOrderOrDefault(entity.getSortOrder()));
        return dataObject;
    }

    public static SubmissionImage toEntity(SubmissionImageDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        SubmissionImage entity = new SubmissionImage();
        entity.setId(SubmissionImageIdCodec.toDomain(dataObject.getId()));
        entity.setSubmissionId(SubmissionIdCodec.toDomain(dataObject.getSubmissionId()));
        entity.setStorageObjectId(StoredObjectIdCodec.toDomain(dataObject.getStorageObjectId()));
        entity.setSortOrder(sortOrderOrDefault(dataObject.getSortOrder()));
        return entity;
    }

    public static List<SubmissionImage> toEntityList(List<SubmissionImageDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<SubmissionImage> entities = new ArrayList<>();
        for (SubmissionImageDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }

    private static int sortOrderOrDefault(Integer sortOrder) {
        return sortOrder == null || sortOrder < 0 ? 0 : sortOrder;
    }
}
