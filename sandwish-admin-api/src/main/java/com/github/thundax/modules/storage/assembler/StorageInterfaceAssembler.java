package com.github.thundax.modules.storage.assembler;

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
import com.github.thundax.modules.storage.entity.valueobject.MultipartUploadPartIdCodec;
import com.github.thundax.modules.storage.entity.valueobject.MultipartUploadSessionIdCodec;
import com.github.thundax.modules.storage.entity.valueobject.StoredObjectIdCodec;
import com.github.thundax.modules.storage.service.query.StorageQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class StorageInterfaceAssembler {
    private StorageInterfaceAssembler() {}

    @NonNull
    public static StorageUploadResponse toUploadResponse(StoredObject entity, StorageConverter storageConverter) {
        if (entity == null) {
            return StorageUploadResponse.builder().build();
        }
        return StorageUploadResponse.builder()
                .id(StoredObjectIdCodec.toStringValue(entity.getId()))
                .originalFilename(entity.getOriginalFileName())
                .extendName(entity.getExtendName())
                .contentType(entity.getContentType())
                .contentUrl(storageConverter.toPreviewUrl(entity))
                .build();
    }

    @NonNull
    public static StorageUploadResponse toUploadErrorResponse(String error) {
        return StorageUploadResponse.builder().error(error).build();
    }

    @NonNull
    public static StorageUploadResponse toUploadEmptyResponse() {
        return StorageUploadResponse.builder().build();
    }

    @NonNull
    public static StorageResponse toResponse(StoredObject entity, StorageConverter storageConverter) {
        if (entity == null) {
            return StorageResponse.builder().build();
        }
        return StorageResponse.builder()
                .id(StoredObjectIdCodec.toStringValue(entity.getId()))
                .originalFilename(entity.getOriginalFilename())
                .extendName(entity.getExtendName())
                .contentType(entity.getContentType())
                .ownerId(entity.getOwnerId())
                .ownerType(
                        entity.getOwnerType() == null
                                ? null
                                : entity.getOwnerType().value())
                .objectStatus(
                        entity.getObjectStatus() == null
                                ? null
                                : entity.getObjectStatus().value())
                .referenceStatus(
                        entity.getReferenceStatus() == null
                                ? null
                                : entity.getReferenceStatus().value())
                .remarks(entity.getRemarks())
                .contentUrl(storageConverter.toPreviewUrl(entity))
                .build();
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
        return StorageTreeNodeResponse.builder()
                .id(businessType)
                .parentId("ROOT")
                .name(businessType)
                .build();
    }

    @NonNull
    public static MultipartUploadSessionResponse toMultipartSessionResponse(MultipartUploadSession entity) {
        if (entity == null) {
            return MultipartUploadSessionResponse.builder().build();
        }
        return MultipartUploadSessionResponse.builder()
                .id(MultipartUploadSessionIdCodec.toStringValue(entity.getId()))
                .uploadId(entity.getUploadId())
                .uploadStatus(
                        entity.getUploadStatus() == null
                                ? null
                                : entity.getUploadStatus().value())
                .uploadedPartCount(entity.getUploadedPartCount())
                .build();
    }

    @NonNull
    public static MultipartUploadPartResponse toMultipartPartResponse(MultipartUploadPart entity) {
        if (entity == null) {
            return MultipartUploadPartResponse.builder().build();
        }
        return MultipartUploadPartResponse.builder()
                .id(MultipartUploadPartIdCodec.toStringValue(entity.getId()))
                .uploadId(entity.getUploadId())
                .partNumber(entity.getPartNumber())
                .etag(entity.getEtag())
                .size(entity.getSize())
                .build();
    }
}
