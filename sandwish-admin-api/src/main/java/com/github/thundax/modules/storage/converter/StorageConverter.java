package com.github.thundax.modules.storage.converter;

import com.github.thundax.autoconfigure.VltavaProperties;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.service.StorageService;
import com.github.thundax.modules.storage.utils.MetaFile;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class StorageConverter {

    private final String servletPath;
    private final StorageService storageService;

    public StorageConverter(VltavaProperties properties, StorageService storageService) {
        this.servletPath = properties.getUpload().getServletPath();
        this.storageService = storageService;
    }

    public String toPreviewUrl(Storage entity) {
        return StringUtils.isBlank(entity.getAccessEndpoint())
                ? this.servletPath + entity.getFileName()
                : entity.getAccessEndpoint();
    }

    public Storage toEntity(String previewUrl) {
        if (!StringUtils.contains(previewUrl, servletPath)) {
            return null;
        }

        String[] names = StringUtils.split(previewUrl, MetaFile.SEPARATOR);
        String filename = names[names.length - 1];

        String[] parts = StringUtils.split(filename, ".");
        return storageService.getById(EntityIdCodec.toDomain(parts[0]));
    }
}
