package com.github.thundax.modules.storage.persistence.assembler;

import com.github.thundax.modules.storage.entity.MultipartUploadPart;
import com.github.thundax.modules.storage.entity.MultipartUploadSession;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.StoredObjectReference;
import com.github.thundax.modules.storage.entity.enums.MultipartUploadStatus;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.enums.StorageType;
import com.github.thundax.modules.storage.entity.enums.StoredObjectReferenceStatus;
import com.github.thundax.modules.storage.entity.enums.StoredObjectStatus;
import com.github.thundax.modules.storage.entity.valueobject.MultipartUploadPartIdCodec;
import com.github.thundax.modules.storage.entity.valueobject.MultipartUploadSessionIdCodec;
import com.github.thundax.modules.storage.entity.valueobject.StoredObjectIdCodec;
import com.github.thundax.modules.storage.persistence.dataobject.MultipartUploadPartDO;
import com.github.thundax.modules.storage.persistence.dataobject.MultipartUploadSessionDO;
import com.github.thundax.modules.storage.persistence.dataobject.StoredObjectDO;
import com.github.thundax.modules.storage.persistence.dataobject.StoredObjectReferenceDO;
import java.util.ArrayList;
import java.util.List;

public final class StoragePersistenceAssembler {

    private StoragePersistenceAssembler() {}

    public static StoredObjectDO toDataObject(StoredObject entity) {
        if (entity == null) {
            return null;
        }
        StoredObjectDO dataObject = new StoredObjectDO();
        dataObject.setId(StoredObjectIdCodec.toValue(entity.getId()));
        dataObject.setName(entity.getName());
        dataObject.setExtendName(entity.getExtendName());
        dataObject.setMimeType(entity.getMimeType());
        dataObject.setOwnerId(entity.getOwnerId());
        dataObject.setOwnerType(ownerTypeValue(entity.getOwnerType()));
        dataObject.setStorageType(storageTypeValue(entity.getStorageType()));
        dataObject.setBucketName(entity.getBucketName());
        dataObject.setObjectKey(entity.getObjectKey());
        dataObject.setSize(entity.getSize());
        dataObject.setAccessEndpoint(entity.getAccessEndpoint());
        dataObject.setObjectStatus(statusValue(entity.getStatus()));
        dataObject.setReferenceStatus(referenceStatusValue(entity.getReferenceStatus()));
        dataObject.setPriority(priorityOrDefault(entity.getPriority()));
        dataObject.setRemarks(entity.getRemarks());
        return dataObject;
    }

    public static StoredObject toEntity(StoredObjectDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        StoredObject entity = new StoredObject();
        entity.setId(StoredObjectIdCodec.toDomain(dataObject.getId()));
        entity.setName(dataObject.getName());
        entity.setExtendName(dataObject.getExtendName());
        entity.setMimeType(dataObject.getMimeType());
        entity.setOwnerId(dataObject.getOwnerId());
        entity.setOwnerType(ownerTypeFrom(dataObject.getOwnerType()));
        entity.setStorageType(storageTypeFrom(dataObject.getStorageType()));
        entity.setBucketName(dataObject.getBucketName());
        entity.setObjectKey(dataObject.getObjectKey());
        entity.setSize(dataObject.getSize());
        entity.setAccessEndpoint(dataObject.getAccessEndpoint());
        entity.setStatus(statusFrom(dataObject.getObjectStatus()));
        entity.setReferenceStatus(referenceStatusFrom(dataObject.getReferenceStatus()));
        entity.setPriority(priorityOrDefault(dataObject.getPriority()));
        entity.setRemarks(dataObject.getRemarks());
        return entity;
    }

    public static List<StoredObject> toEntityList(List<StoredObjectDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<StoredObject> entities = new ArrayList<>();
        for (StoredObjectDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }

    private static int priorityOrDefault(Integer priority) {
        return priority == null || priority < 0 ? 0 : priority;
    }

    private static String ownerTypeValue(StorageOwnerType ownerType) {
        return ownerType == null ? null : ownerType.value();
    }

    private static StorageOwnerType ownerTypeFrom(String ownerType) {
        return ownerType == null ? null : StorageOwnerType.from(ownerType);
    }

    private static String storageTypeValue(StorageType storageType) {
        return storageType == null ? null : storageType.value();
    }

    private static StorageType storageTypeFrom(String storageType) {
        return storageType == null ? null : StorageType.from(storageType);
    }

    private static String statusValue(StoredObjectStatus status) {
        return status == null ? null : status.value();
    }

    private static StoredObjectStatus statusFrom(String status) {
        return status == null ? null : StoredObjectStatus.from(status);
    }

    private static String referenceStatusValue(StoredObjectReferenceStatus referenceStatus) {
        return referenceStatus == null ? null : referenceStatus.value();
    }

    private static StoredObjectReferenceStatus referenceStatusFrom(String referenceStatus) {
        return referenceStatus == null ? null : StoredObjectReferenceStatus.from(referenceStatus);
    }

    public static StoredObjectReferenceDO toBusinessDataObject(StoredObjectReference entity) {
        if (entity == null) {
            return null;
        }
        StoredObjectReferenceDO dataObject = new StoredObjectReferenceDO();
        dataObject.setFileId(StoredObjectIdCodec.toValue(entity.getId()));
        dataObject.setReferenceOwnerId(entity.getBusinessId());
        dataObject.setReferenceOwnerType(entity.getBusinessType());
        dataObject.setBusinessParams(entity.getBusinessParams());
        dataObject.setReferenceStatus(referenceStatusValue(entity.getReferenceStatus()));
        return dataObject;
    }

    public static StoredObjectReference toBusinessEntity(StoredObjectReferenceDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        StoredObjectReference entity = new StoredObjectReference();
        entity.setId(StoredObjectIdCodec.toDomain(dataObject.getFileId()));
        entity.setBusinessId(dataObject.getReferenceOwnerId());
        entity.setBusinessType(dataObject.getReferenceOwnerType());
        entity.setBusinessParams(dataObject.getBusinessParams());
        entity.setReferenceStatus(referenceStatusFrom(dataObject.getReferenceStatus()));
        return entity;
    }

    public static List<StoredObjectReferenceDO> toBusinessDataObjectList(List<StoredObjectReference> entities) {
        if (entities == null) {
            return null;
        }
        List<StoredObjectReferenceDO> dataObjects = new ArrayList<>();
        for (StoredObjectReference entity : entities) {
            dataObjects.add(toBusinessDataObject(entity));
        }
        return dataObjects;
    }

    public static List<StoredObjectReference> toBusinessEntityList(List<StoredObjectReferenceDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<StoredObjectReference> entities = new ArrayList<>();
        for (StoredObjectReferenceDO dataObject : dataObjects) {
            entities.add(toBusinessEntity(dataObject));
        }
        return entities;
    }

    public static MultipartUploadSessionDO toMultipartSessionDataObject(MultipartUploadSession entity) {
        if (entity == null) {
            return null;
        }
        MultipartUploadSessionDO dataObject = new MultipartUploadSessionDO();
        dataObject.setId(MultipartUploadSessionIdCodec.toValue(entity.getId()));
        dataObject.setUploadId(entity.getUploadId());
        dataObject.setOwnerId(entity.getOwnerId());
        dataObject.setOwnerType(ownerTypeValue(entity.getOwnerType()));
        dataObject.setBusinessType(entity.getBusinessType());
        dataObject.setOriginalFilename(entity.getOriginalFilename());
        dataObject.setMimeType(entity.getMimeType());
        dataObject.setStorageType(storageTypeValue(entity.getStorageType()));
        dataObject.setBucketName(entity.getBucketName());
        dataObject.setObjectKey(entity.getObjectKey());
        dataObject.setProviderUploadId(entity.getProviderUploadId());
        dataObject.setTotalSize(entity.getTotalSize());
        dataObject.setPartSize(entity.getPartSize());
        dataObject.setUploadedPartCount(uploadedPartCountOrDefault(entity.getUploadedPartCount()));
        dataObject.setUploadStatus(uploadStatusValue(entity.getUploadStatus()));
        dataObject.setCompletedDate(entity.getCompletedDate());
        dataObject.setAbortedDate(entity.getAbortedDate());
        return dataObject;
    }

    public static MultipartUploadSession toMultipartSessionEntity(MultipartUploadSessionDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        MultipartUploadSession entity = new MultipartUploadSession();
        entity.setId(MultipartUploadSessionIdCodec.toDomain(dataObject.getId()));
        entity.setUploadId(dataObject.getUploadId());
        entity.setOwnerId(dataObject.getOwnerId());
        entity.setOwnerType(ownerTypeFrom(dataObject.getOwnerType()));
        entity.setBusinessType(dataObject.getBusinessType());
        entity.setOriginalFilename(dataObject.getOriginalFilename());
        entity.setMimeType(dataObject.getMimeType());
        entity.setStorageType(storageTypeFrom(dataObject.getStorageType()));
        entity.setBucketName(dataObject.getBucketName());
        entity.setObjectKey(dataObject.getObjectKey());
        entity.setProviderUploadId(dataObject.getProviderUploadId());
        entity.setTotalSize(dataObject.getTotalSize());
        entity.setPartSize(dataObject.getPartSize());
        entity.setUploadedPartCount(uploadedPartCountOrDefault(dataObject.getUploadedPartCount()));
        entity.setUploadStatus(uploadStatusFrom(dataObject.getUploadStatus()));
        entity.setCompletedDate(dataObject.getCompletedDate());
        entity.setAbortedDate(dataObject.getAbortedDate());
        return entity;
    }

    public static MultipartUploadPartDO toMultipartPartDataObject(MultipartUploadPart entity) {
        if (entity == null) {
            return null;
        }
        MultipartUploadPartDO dataObject = new MultipartUploadPartDO();
        dataObject.setId(MultipartUploadPartIdCodec.toValue(entity.getId()));
        dataObject.setUploadId(entity.getUploadId());
        dataObject.setPartNumber(entity.getPartNumber());
        dataObject.setEtag(entity.getEtag());
        dataObject.setSize(entity.getSize());
        return dataObject;
    }

    public static MultipartUploadPart toMultipartPartEntity(MultipartUploadPartDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        MultipartUploadPart entity = new MultipartUploadPart();
        entity.setId(MultipartUploadPartIdCodec.toDomain(dataObject.getId()));
        entity.setUploadId(dataObject.getUploadId());
        entity.setPartNumber(dataObject.getPartNumber());
        entity.setEtag(dataObject.getEtag());
        entity.setSize(dataObject.getSize());
        return entity;
    }

    private static Integer uploadedPartCountOrDefault(Integer uploadedPartCount) {
        return uploadedPartCount == null || uploadedPartCount < 0 ? 0 : uploadedPartCount;
    }

    private static String uploadStatusValue(MultipartUploadStatus uploadStatus) {
        return uploadStatus == null ? null : uploadStatus.value();
    }

    private static MultipartUploadStatus uploadStatusFrom(String uploadStatus) {
        return uploadStatus == null ? null : MultipartUploadStatus.from(uploadStatus);
    }
}
