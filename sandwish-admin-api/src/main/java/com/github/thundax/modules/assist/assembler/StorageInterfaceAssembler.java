package com.github.thundax.modules.assist.assembler;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.assist.response.StorageTreeNodeResponse;
import com.github.thundax.modules.assist.response.StorageUploadResponse;
import com.github.thundax.modules.storage.converter.StorageConverter;
import com.github.thundax.modules.storage.entity.Storage;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class StorageInterfaceAssembler {

    private final StorageConverter storageConverter;

    public StorageInterfaceAssembler(StorageConverter storageConverter) {
        this.storageConverter = storageConverter;
    }

    public EntityId toEntityId(String id) {
        return EntityIdCodec.toDomain(id);
    }

    @NonNull
    public StorageUploadResponse toUploadResponse(Storage entity) {
        if (entity == null) {
            return new StorageUploadResponse();
        }

        StorageUploadResponse response = new StorageUploadResponse();
        response.setId(entity.getId());
        response.setName(entity.getOriginalFileName());
        response.setExtendName(entity.getExtendName());
        response.setMimeType(entity.getMimeType());
        response.setUrl(storageConverter.toPreviewUrl(entity));
        return response;
    }

    @NonNull
    public StorageUploadResponse toUploadErrorResponse(String error) {
        StorageUploadResponse response = new StorageUploadResponse();
        response.setError(error);
        return response;
    }

    @NonNull
    public StorageTreeNodeResponse toBusinessTypeTreeNode(String businessType) {
        StorageTreeNodeResponse response = new StorageTreeNodeResponse();
        response.setId(businessType);
        response.setParentId("ROOT");
        response.setName(businessType);
        return response;
    }
}
