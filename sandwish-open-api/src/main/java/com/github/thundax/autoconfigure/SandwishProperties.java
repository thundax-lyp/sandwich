package com.github.thundax.autoconfigure;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.NonNull;

@ConfigurationProperties(prefix = "sandwish")
public class SandwishProperties {

    @Setter
    private UploadProperties upload;

    @NonNull
    public UploadProperties getUpload() {
        return upload != null ? upload : new UploadProperties();
    }

    public static class UploadProperties {

        @Setter
        private String contentPath;

        @Setter
        private String storagePath;

        @Setter
        private List<String> allowImageSuffix;

        @Setter
        private List<String> allowSuffix;

        @Setter
        private Integer maxFileCount;

        @Setter
        private Long maxFileSize;

        @Setter
        private Float imageQuality;

        public String getContentPath() {
            if (StringUtils.isEmpty(contentPath)) {
                return "/api/storage/object/";
            }
            return contentPath.endsWith("/") ? contentPath : contentPath + "/";
        }

        @NonNull
        public String getStoragePath() {
            if (StringUtils.isBlank(storagePath)) {
                return File.separator;
            } else if (!StringUtils.endsWith(storagePath, File.separator)) {
                return storagePath + File.separator;
            }
            return storagePath;
        }

        @NonNull
        public List<String> getAllowImageSuffix() {
            if (allowImageSuffix == null) {
                return new ArrayList<>();
            }
            return allowImageSuffix;
        }

        @NonNull
        public String getAllowImageSuffixString() {
            return StringUtils.join(getAllowImageSuffix(), ",");
        }

        @NonNull
        public List<String> getAllowSuffix() {
            if (allowSuffix == null) {
                return new ArrayList<>();
            }
            return allowSuffix;
        }

        @NonNull
        public String getAllowSuffixString() {
            return StringUtils.join(getAllowSuffix(), ",");
        }

        @NonNull
        public Integer getMaxFileCount() {
            return maxFileCount == null || maxFileCount < 0 ? 10 : maxFileCount;
        }

        @NonNull
        public Long getMaxFileSize() {
            return maxFileSize == null || maxFileSize < 0L ? 20971520L : maxFileSize;
        }

        @NonNull
        public Float getImageQuality() {
            return imageQuality == null || imageQuality < 0.5 ? 0.8f : imageQuality;
        }
    }
}
