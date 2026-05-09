package com.github.thundax.modules.storage.assembler;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.storage.controller.request.StoragePageRequest;
import com.github.thundax.modules.storage.controller.response.MultipartUploadPartResponse;
import com.github.thundax.modules.storage.controller.response.MultipartUploadSessionResponse;
import com.github.thundax.modules.storage.controller.response.StorageResponse;
import com.github.thundax.modules.storage.controller.response.StorageTreeNodeResponse;
import com.github.thundax.modules.storage.controller.response.StorageUploadResponse;
import com.github.thundax.modules.storage.converter.StorageConverter;
import com.github.thundax.modules.storage.entity.MultipartUploadPart;
import com.github.thundax.modules.storage.entity.MultipartUploadSession;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.enums.StoredObjectReferenceStatus;
import com.github.thundax.modules.storage.entity.enums.StoredObjectStatus;
import com.github.thundax.modules.storage.service.query.StorageQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class StorageInterfaceAssembler {
    private StorageInterfaceAssembler() {}

    @NonNull
    public static StorageUploadResponse toUploadResponse(StoredObject entity, StorageConverter storageConverter) {
        if (entity == null) {
            return new StorageUploadResponse();
        }
        StorageUploadResponse response = new StorageUploadResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setOriginalFilename(entity.getOriginalFileName());
        response.setExtendName(entity.getExtendName());
        response.setContentType(entity.getContentType());
        response.setContentUrl(storageConverter.toPreviewUrl(entity));
        return response;
    }

    @NonNull
    public static StorageUploadResponse toUploadErrorResponse(String error) {
        StorageUploadResponse response = new StorageUploadResponse();
        response.setError(error);
        return response;
    }

    @NonNull
    public static StorageResponse toResponse(StoredObject entity, StorageConverter storageConverter) {
        if (entity == null) {
            return new StorageResponse();
        }
        StorageResponse response = new StorageResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setOriginalFilename(entity.getOriginalFilename());
        response.setExtendName(entity.getExtendName());
        response.setContentType(entity.getContentType());
        response.setOwnerId(entity.getOwnerId());
        response.setOwnerType(
                entity.getOwnerType() == null ? null : entity.getOwnerType().value());
        response.setObjectStatus(
                entity.getObjectStatus() == null
                        ? null
                        : entity.getObjectStatus().value());
        response.setReferenceStatus(
                entity.getReferenceStatus() == null
                        ? null
                        : entity.getReferenceStatus().value());
        response.setPriority(entity.getPriority());
        response.setRemarks(entity.getRemarks());
        response.setContentUrl(storageConverter.toPreviewUrl(entity));
        return response;
    }

    @NonNull
    public static StorageQuery toQuery(@NonNull StoragePageRequest request) {
        StorageQuery query = new StorageQuery();
        query.setContentType(request.getContentType());
        query.setObjectStatus(
                StringUtils.isBlank(request.getObjectStatus())
                        ? null
                        : StoredObjectStatus.from(request.getObjectStatus()));
        query.setReferenceStatus(
                StringUtils.isBlank(request.getReferenceStatus())
                        ? null
                        : StoredObjectReferenceStatus.from(request.getReferenceStatus()));
        query.setOriginalFilename(request.getOriginalFilename());
        query.setRemarks(request.getRemarks());
        return query;
    }

    @NonNull
    public static StorageTreeNodeResponse toBusinessTypeTreeNode(String businessType) {
        StorageTreeNodeResponse response = new StorageTreeNodeResponse();
        response.setId(businessType);
        response.setParentId("ROOT");
        response.setName(businessType);
        return response;
    }

    @NonNull
    public static MultipartUploadSessionResponse toMultipartSessionResponse(MultipartUploadSession entity) {
        if (entity == null) {
            return new MultipartUploadSessionResponse();
        }
        MultipartUploadSessionResponse response = new MultipartUploadSessionResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setUploadId(entity.getUploadId());
        response.setUploadStatus(
                entity.getUploadStatus() == null
                        ? null
                        : entity.getUploadStatus().value());
        response.setUploadedPartCount(entity.getUploadedPartCount());
        return response;
    }

    @NonNull
    public static MultipartUploadPartResponse toMultipartPartResponse(MultipartUploadPart entity) {
        if (entity == null) {
            return new MultipartUploadPartResponse();
        }
        MultipartUploadPartResponse response = new MultipartUploadPartResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setUploadId(entity.getUploadId());
        response.setPartNumber(entity.getPartNumber());
        response.setEtag(entity.getEtag());
        response.setSize(entity.getSize());
        return response;
    }
}
