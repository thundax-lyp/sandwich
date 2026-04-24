package com.github.thundax.autoconfigure;

import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.common.utils.StringUtils;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.NonNull;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.List;

/**
 * @author thundax
 */
@ConfigurationProperties(prefix = "vltava")
public class VltavaProperties {

    private static final int DEFAULT_LOG_ALIVE_DAYS = 90;

    private String whiteCaptcha;

    private ProductProperties product;
    private LogProperties log;
    private UploadProperties upload;
    private V2ClientProperties v2Client;

    private MailProperties mail;

    private ResponseWrapperFilterProperties responseWrapperFilter;
    private AccessTokenFilterProperties accessTokenFilter;

    private static VltavaProperties instance;

    public static VltavaProperties getInstance() {
        return instance;
    }

    public VltavaProperties() {
        instance = this;
    }

    public String getWhiteCaptcha() {
        return whiteCaptcha;
    }

    public void setWhiteCaptcha(String whiteCaptcha) {
        this.whiteCaptcha = whiteCaptcha;
    }

    @NonNull
    public ProductProperties getProduct() {
        return product != null ? product : new ProductProperties();
    }

    public void setProduct(ProductProperties productProperties) {
        this.product = productProperties;
    }

    @NonNull
    public LogProperties getLog() {
        return log != null ? log : new LogProperties();
    }

    public void setLog(LogProperties log) {
        this.log = log;
    }

    @NonNull
    public UploadProperties getUpload() {
        return upload != null ? upload : new UploadProperties();
    }

    public void setUpload(UploadProperties upload) {
        this.upload = upload;
    }

    @NotNull
    public V2ClientProperties getV2Client() {
        return v2Client != null ? v2Client : new V2ClientProperties();
    }

    public void setV2Client(V2ClientProperties v2Client) {
        this.v2Client = v2Client;
    }

    @NotNull
    public MailProperties getMail() {
        return mail != null ? mail : new MailProperties();
    }

    public void setMail(MailProperties mail) {
        this.mail = mail;
    }

    @NotNull
    public ResponseWrapperFilterProperties getResponseWrapperFilter() {
        return responseWrapperFilter != null ? responseWrapperFilter : new ResponseWrapperFilterProperties();
    }

    public void setResponseWrapperFilter(ResponseWrapperFilterProperties responseWrapperFilter) {
        this.responseWrapperFilter = responseWrapperFilter;
    }

    @NotNull
    public AccessTokenFilterProperties getAccessTokenFilter() {
        return accessTokenFilter != null ? accessTokenFilter : new AccessTokenFilterProperties();
    }

    public void setAccessTokenFilter(AccessTokenFilterProperties accessTokenFilter) {
        this.accessTokenFilter = accessTokenFilter;
    }

    /**
     * 产品名/版本
     */
    public static class ProductProperties {

        private String name;
        private String shortName;
        private String plainName;
        private String version;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getShortName() {
            return shortName;
        }

        public void setShortName(String shortName) {
            this.shortName = shortName;
        }

        public String getPlainName() {
            return plainName;
        }

        public void setPlainName(String plainName) {
            this.plainName = plainName;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }


    /**
     * 上传文件/图片设置
     */
    public static class UploadProperties {
        //本地存储目录
        private String servletPath;
        //本地存储目录
        private String storagePath;
        //可上传的图片文件名后缀
        private List<String> allowImageSuffix;
        //可上传的文件名后缀
        private List<String> allowSuffix;
        //最大同时上传文件数
        private Integer maxFileCount;
        //最大上传单个文件大小
        private Long maxFileSize;
        //图片压缩质量
        private Float imageQuality;

        public String getServletPath() {
            if (StringUtils.isEmpty(servletPath)) {
                return "/servlet/storage/";
            }

            return servletPath.endsWith("/") ? servletPath : servletPath + "/";
        }

        public void setServletPath(String servletPath) {
            this.servletPath = servletPath;
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

        public void setStoragePath(String storagePath) {
            this.storagePath = storagePath;
        }

        @NonNull
        public List<String> getAllowImageSuffix() {
            if (allowImageSuffix == null) {
                return ListUtils.newArrayList();
            }
            return allowImageSuffix;
        }

        @NonNull
        public String getAllowImageSuffixString() {
            return StringUtils.join(getAllowImageSuffix(), ",");
        }

        public void setAllowImageSuffix(List<String> allowImageSuffix) {
            this.allowImageSuffix = allowImageSuffix;
        }

        @NonNull
        public List<String> getAllowSuffix() {
            if (allowSuffix == null) {
                return ListUtils.newArrayList();
            }
            return allowSuffix;
        }

        @NonNull
        public String getAllowSuffixString() {
            return StringUtils.join(getAllowSuffix(), ",");
        }

        public void setAllowSuffix(List<String> allowSuffix) {
            this.allowSuffix = allowSuffix;
        }

        @NonNull
        public Integer getMaxFileCount() {
            return maxFileCount == null || maxFileCount < 0 ? 10 : maxFileCount;
        }

        public void setMaxFileCount(Integer maxFileCount) {
            this.maxFileCount = maxFileCount;
        }

        @NonNull
        public Long getMaxFileSize() {
            return maxFileSize == null || maxFileSize < 0L ? 20971520L : maxFileSize;
        }

        public void setMaxFileSize(Long maxFileSize) {
            this.maxFileSize = maxFileSize;
        }

        @NonNull
        public Float getImageQuality() {
            return imageQuality == null || imageQuality < 0.5 ? 0.8f : imageQuality;
        }

        public void setImageQuality(Float imageQuality) {
            this.imageQuality = imageQuality;
        }
    }


    /**
     * 日志
     */
    public static class LogProperties {
        // 本地存储目录
        private String storagePath;
        // 数据库中保存天数
        private Integer aliveDays;

        @NonNull
        public String getStoragePath() {
            if (StringUtils.isBlank(storagePath)) {
                return File.separator;
            } else if (!StringUtils.endsWith(storagePath, File.separator)) {
                return storagePath + File.separator;
            }
            return storagePath;
        }

        public void setStoragePath(String storagePath) {
            this.storagePath = storagePath;
        }

        @NonNull
        public Integer getAliveDays() {
            return aliveDays == null ? DEFAULT_LOG_ALIVE_DAYS : aliveDays;
        }

        public void setAliveDays(Integer aliveDays) {
            this.aliveDays = aliveDays;
        }
    }


    public static class ResponseWrapperFilterProperties {

        private List<String> urlPatterns;
        private List<String> excludePath;

        public List<String> getUrlPatterns() {
            return urlPatterns;
        }

        public void setUrlPatterns(List<String> urlPatterns) {
            this.urlPatterns = urlPatterns;
        }

        public List<String> getExcludePath() {
            return excludePath;
        }

        public void setExcludePath(List<String> excludePath) {
            this.excludePath = excludePath;
        }

    }


    public static class AccessTokenFilterProperties {

        private List<String> urlPatterns;
        private List<String> excludePath;

        public List<String> getUrlPatterns() {
            return urlPatterns;
        }

        public void setUrlPatterns(List<String> urlPatterns) {
            this.urlPatterns = urlPatterns;
        }

        public List<String> getExcludePath() {
            return excludePath;
        }

        public void setExcludePath(List<String> excludePath) {
            this.excludePath = excludePath;
        }

    }


    public static class V2ClientProperties {
        private String serviceUrl;
        private Integer appId;
        private String appSecret;

        public String getServiceUrl() {
            return serviceUrl;
        }

        public void setServiceUrl(String serviceUrl) {
            this.serviceUrl = serviceUrl;
        }

        public Integer getAppId() {
            return appId;
        }

        public void setAppId(Integer appId) {
            this.appId = appId;
        }

        public String getAppSecret() {
            return appSecret;
        }

        public void setAppSecret(String appSecret) {
            this.appSecret = appSecret;
        }
    }

}
