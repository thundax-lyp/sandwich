package com.github.thundax.modules.storage.assembler;

import com.github.thundax.modules.storage.controller.response.StorageUploadResponse;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.valueobject.StoredObjectIdCodec;
import com.github.thundax.modules.storage.helper.StorageUploadResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

public final class StorageInterfaceAssembler {

    private static final String DEFAULT_CONTEXT_PATH = "/open-api";

    private StorageInterfaceAssembler() {}

    @NonNull
    public static StorageUploadResponse toUploadResponse(StoredObject entity, String contentPath) {
        if (entity == null) {
            return StorageUploadResponse.builder().build();
        }
        return StorageUploadResponse.builder()
                .id(StoredObjectIdCodec.toStringValue(entity.getId()))
                .originalFilename(entity.getOriginalFilename())
                .extendName(entity.getExtendName())
                .contentType(entity.getContentType())
                .contentUrl(toContentUrl(entity, contentPath))
                .build();
    }

    @NonNull
    public static StorageUploadResponse toUploadResponse(StorageUploadResult result, String contentPath) {
        if (result == null) {
            return toUploadEmptyResponse();
        }
        if (result.hasError()) {
            return toUploadErrorResponse(result.getError());
        }
        return toUploadResponse(result.getStorage(), contentPath);
    }

    @NonNull
    public static StorageUploadResponse toUploadErrorResponse(String error) {
        return StorageUploadResponse.builder().error(error).build();
    }

    @NonNull
    public static StorageUploadResponse toUploadEmptyResponse() {
        return StorageUploadResponse.builder().build();
    }

    private static String toContentUrl(StoredObject entity, String contentPath) {
        String path = StringUtils.isBlank(entity.getAccessEndpoint())
                ? contentPath + StoredObjectIdCodec.toValue(entity.getId()) + "/content"
                : entity.getAccessEndpoint();
        return withContextPath(path);
    }

    private static String withContextPath(String path) {
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

    private static String currentContextPath() {
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
