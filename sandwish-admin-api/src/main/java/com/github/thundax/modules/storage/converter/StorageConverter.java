package com.github.thundax.modules.storage.converter;

import com.github.thundax.autoconfigure.SandwishProperties;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.valueobject.StoredObjectIdCodec;
import com.github.thundax.modules.storage.service.StorageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class StorageConverter {

    private static final String DEFAULT_CONTEXT_PATH = "/admin-api";

    private final String contentPath;
    private final StorageService storageService;

    public StorageConverter(SandwishProperties properties, StorageService storageService) {
        this.contentPath = properties.getUpload().getContentPath();
        this.storageService = storageService;
    }

    public String toPreviewUrl(StoredObject entity) {
        String previewPath = StringUtils.isBlank(entity.getAccessEndpoint())
                ? this.contentPath + StoredObjectIdCodec.toValue(entity.getId()) + "/content"
                : entity.getAccessEndpoint();
        return withContextPath(previewPath);
    }

    public StoredObject toEntity(String previewUrl) {
        if (!StringUtils.contains(previewUrl, contentPath)) {
            return null;
        }

        String objectPath = StringUtils.substringBefore(StringUtils.substringAfter(previewUrl, contentPath), "?");
        String objectId = StringUtils.removeEnd(objectPath, "/content");
        return storageService.get(StoredObjectIdCodec.toDomain(Long.valueOf(objectId)));
    }

    private String withContextPath(String path) {
        if (StringUtils.isBlank(path)
                || StringUtils.startsWithIgnoreCase(path, "http://")
                || StringUtils.startsWithIgnoreCase(path, "https://")) {
            return path;
        }
        String contextPath = currentContextPath();
        if (StringUtils.startsWith(path, contextPath + "/")) {
            return path;
        }
        return UriComponentsBuilder.fromPath(contextPath).path(path).build().toUriString();
    }

    private String currentContextPath() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes) {
            String contextPath =
                    ((ServletRequestAttributes) attributes).getRequest().getContextPath();
            if (StringUtils.isNotBlank(contextPath)) {
                return contextPath;
            }
        }
        return DEFAULT_CONTEXT_PATH;
    }
}
