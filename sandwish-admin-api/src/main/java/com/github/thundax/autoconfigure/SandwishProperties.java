package com.github.thundax.autoconfigure;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.NonNull;

@ConfigurationProperties(prefix = "sandwish")
public class SandwishProperties {

    private static final int DEFAULT_LOG_ALIVE_DAYS = 90;
    private static final String DEFAULT_LOG_STORAGE_PATH =
            System.getProperty("java.io.tmpdir") + File.separator + "sandwish" + File.separator + "sys-log";

    private static SandwishProperties instance;

    @Setter
    private String whiteCaptcha;

    @Setter
    private ProductProperties product;

    @Setter
    private LogProperties log;

    @Setter
    private UploadProperties upload;

    @Setter
    private V2ClientProperties v2Client;

    @Setter
    private MailProperties mail;

    @Setter
    private ResponseWrapperFilterProperties responseWrapperFilter;

    @Setter
    private AccessTokenFilterProperties accessTokenFilter;

    public SandwishProperties() {
        instance = this;
    }

    public static SandwishProperties getInstance() {
        return instance;
    }

    public String getWhiteCaptcha() {
        return whiteCaptcha;
    }

    @NonNull
    public ProductProperties getProduct() {
        return product != null ? product : new ProductProperties();
    }

    @NonNull
    public LogProperties getLog() {
        return log != null ? log : new LogProperties();
    }

    @NonNull
    public UploadProperties getUpload() {
        return upload != null ? upload : new UploadProperties();
    }

    @NotNull
    public V2ClientProperties getV2Client() {
        return v2Client != null ? v2Client : new V2ClientProperties();
    }

    @NotNull
    public MailProperties getMail() {
        return mail != null ? mail : new MailProperties();
    }

    @NotNull
    public ResponseWrapperFilterProperties getResponseWrapperFilter() {
        return responseWrapperFilter != null ? responseWrapperFilter : new ResponseWrapperFilterProperties();
    }

    @NotNull
    public AccessTokenFilterProperties getAccessTokenFilter() {
        return accessTokenFilter != null ? accessTokenFilter : new AccessTokenFilterProperties();
    }

    public static class ProductProperties {

        @Setter
        private String name;

        @Setter
        private String shortName;

        @Setter
        private String plainName;

        @Setter
        private String version;

        public String getName() {
            return name;
        }

        public String getShortName() {
            return shortName;
        }

        public String getPlainName() {
            return plainName;
        }

        public String getVersion() {
            return version;
        }
    }

    public static class UploadProperties {
        // 本地存储目录
        @Setter
        private String contentPath;
        // 本地存储目录
        @Setter
        private String storagePath;
        // 可上传的图片文件名后缀
        @Setter
        private List<String> allowImageSuffix;
        // 可上传的文件名后缀
        @Setter
        private List<String> allowSuffix;
        // 最大同时上传文件数
        @Setter
        private Integer maxFileCount;
        // 最大上传单个文件大小
        @Setter
        private Long maxFileSize;
        // 图片压缩质量
        @Setter
        private Float imageQuality;

        public String getContentPath() {
            if (StringUtils.isEmpty(contentPath)) {
                return "/api/assist/storage/objects/";
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

    public static class LogProperties {
        // 本地存储目录
        @Setter
        private String storagePath;
        // 数据库中保存天数
        @Setter
        private Integer aliveDays;

        @NonNull
        public String getStoragePath() {
            if (StringUtils.isBlank(storagePath)) {
                return DEFAULT_LOG_STORAGE_PATH + File.separator;
            } else if (!StringUtils.endsWith(storagePath, File.separator)) {
                return storagePath + File.separator;
            }
            return storagePath;
        }

        @NonNull
        public Integer getAliveDays() {
            return aliveDays == null ? DEFAULT_LOG_ALIVE_DAYS : aliveDays;
        }
    }

    public static class ResponseWrapperFilterProperties {

        @Setter
        private List<String> urlPatterns;

        @Setter
        private List<String> excludePath;

        public List<String> getUrlPatterns() {
            return urlPatterns;
        }

        public List<String> getExcludePath() {
            if (excludePath == null) {
                return new ArrayList<>();
            }
            return excludePath;
        }
    }

    public static class AccessTokenFilterProperties {

        @Setter
        private List<String> urlPatterns;

        @Setter
        private List<String> excludePath;

        public List<String> getUrlPatterns() {
            return urlPatterns;
        }

        public List<String> getExcludePath() {
            if (excludePath == null) {
                return new ArrayList<>();
            }
            return excludePath;
        }
    }

    public static class V2ClientProperties {
        @Setter
        private String serviceUrl;

        @Setter
        private Integer appId;

        @Setter
        private String appSecret;

        public String getServiceUrl() {
            return serviceUrl;
        }

        public Integer getAppId() {
            return appId;
        }

        public String getAppSecret() {
            return appSecret;
        }
    }
}
