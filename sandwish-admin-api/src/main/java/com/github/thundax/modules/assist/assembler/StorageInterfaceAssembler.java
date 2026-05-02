package com.github.thundax.modules.assist.assembler;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.assist.controller.request.StoragePageRequest;
import com.github.thundax.modules.assist.controller.response.StorageResponse;
import com.github.thundax.modules.assist.controller.response.StorageTreeNodeResponse;
import com.github.thundax.modules.assist.controller.response.StorageUploadResponse;
import com.github.thundax.modules.storage.converter.StorageConverter;
import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.entity.enums.StorageStatus;
import com.github.thundax.modules.storage.entity.enums.StorageVisibility;
import com.github.thundax.modules.storage.service.query.StorageQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class StorageInterfaceAssembler {
    private StorageInterfaceAssembler() {}

    @NonNull
    public static StorageUploadResponse toUploadResponse(Storage entity, StorageConverter storageConverter) {
        if (entity == null) {
            return new StorageUploadResponse();
        }
        StorageUploadResponse response = new StorageUploadResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setName(entity.getOriginalFileName());
        response.setExtendName(entity.getExtendName());
        response.setMimeType(entity.getMimeType());
        response.setUrl(storageConverter.toPreviewUrl(entity));
        return response;
    }

    @NonNull
    public static StorageUploadResponse toUploadErrorResponse(String error) {
        StorageUploadResponse response = new StorageUploadResponse();
        response.setError(error);
        return response;
    }

    @NonNull
    public static StorageResponse toResponse(Storage entity, StorageConverter storageConverter) {
        if (entity == null) {
            return new StorageResponse();
        }
        StorageResponse response = new StorageResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setName(entity.getName());
        response.setExtendName(entity.getExtendName());
        response.setMimeType(entity.getMimeType());
        response.setOwnerId(entity.getOwnerId());
        response.setOwnerType(
                entity.getOwnerType() == null ? null : entity.getOwnerType().value());
        response.setStatus(
                entity.getStatus() == null ? null : entity.getStatus().value());
        response.setVisibility(
                entity.getVisibility() == null ? null : entity.getVisibility().value());
        response.setPriority(entity.getPriority());
        response.setRemarks(entity.getRemarks());
        response.setCreateDate(entity.getCreateDate());
        response.setUpdateDate(entity.getUpdateDate());
        response.setUrl(storageConverter.toPreviewUrl(entity));
        return response;
    }

    @NonNull
    public static StorageQuery toQuery(@NonNull StoragePageRequest request) {
        StorageQuery query = new StorageQuery();
        query.setMimeType(request.getMimeType());
        query.setStatus(StringUtils.isBlank(request.getStatus()) ? null : StorageStatus.from(request.getStatus()));
        query.setVisibility(
                StringUtils.isBlank(request.getVisibility()) ? null : StorageVisibility.from(request.getVisibility()));
        query.setName(request.getName());
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
}
