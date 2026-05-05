package com.github.thundax.modules.storage.converter;

import com.github.thundax.autoconfigure.SandwishProperties;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.service.StorageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class StorageConverter {

    private final String contentPath;
    private final StorageService storageService;

    public StorageConverter(SandwishProperties properties, StorageService storageService) {
        this.contentPath = properties.getUpload().getContentPath();
        this.storageService = storageService;
    }

    public String toPreviewUrl(StoredObject entity) {
        return StringUtils.isBlank(entity.getAccessEndpoint())
                ? this.contentPath + EntityIdCodec.toValue(entity.getId()) + "/content"
                : entity.getAccessEndpoint();
    }

    public StoredObject toEntity(String previewUrl) {
        if (!StringUtils.contains(previewUrl, contentPath)) {
            return null;
        }

        String objectId = StringUtils.removeEnd(StringUtils.substringAfter(previewUrl, contentPath), "/content");
        return storageService.getById(EntityIdCodec.toDomain(objectId));
    }
}
