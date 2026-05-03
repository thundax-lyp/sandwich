package com.github.thundax.modules.storage.persistence.assembler;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.storage.entity.MultipartUploadPart;
import com.github.thundax.modules.storage.entity.MultipartUploadSession;
import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.entity.StorageBusiness;
import com.github.thundax.modules.storage.entity.enums.MultipartUploadStatus;
import com.github.thundax.modules.storage.entity.enums.StorageBackendType;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.enums.StorageStatus;
import com.github.thundax.modules.storage.entity.enums.StorageVisibility;
import com.github.thundax.modules.storage.persistence.dataobject.MultipartUploadPartDO;
import com.github.thundax.modules.storage.persistence.dataobject.MultipartUploadSessionDO;
import com.github.thundax.modules.storage.persistence.dataobject.StorageBusinessDO;
import com.github.thundax.modules.storage.persistence.dataobject.StorageDO;
import java.util.ArrayList;
import java.util.List;

public final class StoragePersistenceAssembler {

    private StoragePersistenceAssembler() {}

    public static StorageDO toDataObject(Storage entity) {
        if (entity == null) {
            return null;
        }
        StorageDO dataObject = new StorageDO();
        dataObject.setId(EntityIdCodec.toValue(entity.getId()));
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
        dataObject.setEnableFlag(statusValue(entity.getStatus()));
        dataObject.setPublicFlag(visibilityValue(entity.getVisibility()));
        dataObject.setPriority(priorityOrDefault(entity.getPriority()));
        dataObject.setRemarks(entity.getRemarks());
        dataObject.setCreateDate(entity.getCreateDate());
        dataObject.setUpdateDate(entity.getUpdateDate());
        return dataObject;
    }

    public static Storage toEntity(StorageDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        Storage entity = new Storage();
        entity.setId(EntityIdCodec.toDomain(dataObject.getId()));
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
        entity.setStatus(statusFrom(dataObject.getEnableFlag()));
        entity.setVisibility(visibilityFrom(dataObject.getPublicFlag()));
        entity.setPriority(priorityOrDefault(dataObject.getPriority()));
        entity.setRemarks(dataObject.getRemarks());
        entity.setCreateDate(dataObject.getCreateDate());
        entity.setUpdateDate(dataObject.getUpdateDate());
        return entity;
    }

    public static List<Storage> toEntityList(List<StorageDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<Storage> entities = new ArrayList<>();
        for (StorageDO dataObject : dataObjects) {
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

    private static String storageTypeValue(StorageBackendType storageType) {
        return storageType == null ? null : storageType.value();
    }

    private static StorageBackendType storageTypeFrom(String storageType) {
        return storageType == null ? null : StorageBackendType.from(storageType);
    }

    private static String statusValue(StorageStatus status) {
        return status == null ? null : status.value();
    }

    private static StorageStatus statusFrom(String status) {
        return status == null ? null : StorageStatus.from(status);
    }

    private static String visibilityValue(StorageVisibility visibility) {
        return visibility == null ? null : visibility.value();
    }

    private static StorageVisibility visibilityFrom(String visibility) {
        return visibility == null ? null : StorageVisibility.from(visibility);
    }

    public static StorageBusinessDO toBusinessDataObject(StorageBusiness entity) {
        if (entity == null) {
            return null;
        }
        StorageBusinessDO dataObject = new StorageBusinessDO();
        dataObject.setFileId(EntityIdCodec.toValue(entity.getId()));
        dataObject.setBusinessId(entity.getBusinessId());
        dataObject.setBusinessType(entity.getBusinessType());
        dataObject.setBusinessParams(entity.getBusinessParams());
        dataObject.setPublicFlag(visibilityValue(entity.getVisibility()));
        return dataObject;
    }

    public static StorageBusiness toBusinessEntity(StorageBusinessDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        StorageBusiness entity = new StorageBusiness();
        entity.setId(EntityIdCodec.toDomain(dataObject.getFileId()));
        entity.setBusinessId(dataObject.getBusinessId());
        entity.setBusinessType(dataObject.getBusinessType());
        entity.setBusinessParams(dataObject.getBusinessParams());
        entity.setVisibility(visibilityFrom(dataObject.getPublicFlag()));
        return entity;
    }

    public static List<StorageBusinessDO> toBusinessDataObjectList(List<StorageBusiness> entities) {
        if (entities == null) {
            return null;
        }
        List<StorageBusinessDO> dataObjects = new ArrayList<>();
        for (StorageBusiness entity : entities) {
            dataObjects.add(toBusinessDataObject(entity));
        }
        return dataObjects;
    }

    public static List<StorageBusiness> toBusinessEntityList(List<StorageBusinessDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<StorageBusiness> entities = new ArrayList<>();
        for (StorageBusinessDO dataObject : dataObjects) {
            entities.add(toBusinessEntity(dataObject));
        }
        return entities;
    }

    public static MultipartUploadSessionDO toMultipartSessionDataObject(MultipartUploadSession entity) {
        if (entity == null) {
            return null;
        }
        MultipartUploadSessionDO dataObject = new MultipartUploadSessionDO();
        dataObject.setId(EntityIdCodec.toValue(entity.getId()));
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
        dataObject.setCreateDate(entity.getCreateDate());
        dataObject.setUpdateDate(entity.getUpdateDate());
        dataObject.setCompletedDate(entity.getCompletedDate());
        dataObject.setAbortedDate(entity.getAbortedDate());
        return dataObject;
    }

    public static MultipartUploadSession toMultipartSessionEntity(MultipartUploadSessionDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        MultipartUploadSession entity = new MultipartUploadSession();
        entity.setId(EntityIdCodec.toDomain(dataObject.getId()));
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
        entity.setCreateDate(dataObject.getCreateDate());
        entity.setUpdateDate(dataObject.getUpdateDate());
        entity.setCompletedDate(dataObject.getCompletedDate());
        entity.setAbortedDate(dataObject.getAbortedDate());
        return entity;
    }

    public static MultipartUploadPartDO toMultipartPartDataObject(MultipartUploadPart entity) {
        if (entity == null) {
            return null;
        }
        MultipartUploadPartDO dataObject = new MultipartUploadPartDO();
        dataObject.setId(EntityIdCodec.toValue(entity.getId()));
        dataObject.setUploadId(entity.getUploadId());
        dataObject.setPartNumber(entity.getPartNumber());
        dataObject.setEtag(entity.getEtag());
        dataObject.setSize(entity.getSize());
        dataObject.setCreateDate(entity.getCreateDate());
        return dataObject;
    }

    public static MultipartUploadPart toMultipartPartEntity(MultipartUploadPartDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        MultipartUploadPart entity = new MultipartUploadPart();
        entity.setId(EntityIdCodec.toDomain(dataObject.getId()));
        entity.setUploadId(dataObject.getUploadId());
        entity.setPartNumber(dataObject.getPartNumber());
        entity.setEtag(dataObject.getEtag());
        entity.setSize(dataObject.getSize());
        entity.setCreateDate(dataObject.getCreateDate());
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
