package com.github.thundax.modules.storage.converter;

import com.github.thundax.autoconfigure.SandwishProperties;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.service.StorageService;
import com.github.thundax.modules.storage.service.query.StorageQuery;
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
        StorageQuery query = new StorageQuery();
        query.setId(EntityIdCodec.toDomain(Long.valueOf(objectId)));
        return storageService.get(query);
    }
}
