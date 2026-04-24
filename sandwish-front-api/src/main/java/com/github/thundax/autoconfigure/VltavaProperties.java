package com.github.thundax.autoconfigure;

import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.common.utils.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.NonNull;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.List;

/**
 * @author wdit
 */
@ConfigurationProperties(prefix = "vltava")
public class VltavaProperties {

    private String whiteCaptcha;

    private ProductProperties productProperties;
    private UploadProperties uploadProperties;
    private ApiProperties apiProperties;

    private XssFilterProperties xssFilter;

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
        return productProperties != null ? productProperties : new ProductProperties();
    }

    public void setProduct(ProductProperties productProperties) {
        this.productProperties = productProperties;
    }

    @NonNull
    public UploadProperties getUpload() {
        return uploadProperties != null ? uploadProperties : new UploadProperties();
    }

    public void setUpload(UploadProperties uploadProperties) {
        this.uploadProperties = uploadProperties;
    }

    @NotNull
    public ApiProperties getApi() {
        return apiProperties != null ? apiProperties : new ApiProperties();
    }

    public void setApi(ApiProperties apiProperties) {
        this.apiProperties = apiProperties;
    }

    @NotNull
    public XssFilterProperties getXssFilter() {
        return xssFilter != null ? xssFilter : new XssFilterProperties();
    }

    public void setXssFilter(XssFilterProperties xssFilter) {
        this.xssFilter = xssFilter;
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

    public static class ApiProperties {

        private String adviceUrlFormat;

        public String getAdviceUrlFormat() {
            return adviceUrlFormat;
        }

        public void setAdviceUrlFormat(String adviceUrlFormat) {
            this.adviceUrlFormat = adviceUrlFormat;
        }
    }


    public static class XssFilterProperties {
        private String enabled;
        private String tagExcludes;
        private String tagIncludes;
        private String urlPatterns;
        private String urlExcludes;

        public String getEnabled() {
            return enabled;
        }

        public void setEnabled(String enabled) {
            this.enabled = enabled;
        }

        public String getTagExcludes() {
            return tagExcludes;
        }

        public void setTagExcludes(String tagExcludes) {
            this.tagExcludes = tagExcludes;
        }

        public String getTagIncludes() {
            return tagIncludes;
        }

        public void setTagIncludes(String tagIncludes) {
            this.tagIncludes = tagIncludes;
        }

        public String getUrlPatterns() {
            return StringUtils.isNotBlank(urlPatterns) ? urlPatterns : "/*";
        }

        public void setUrlPatterns(String urlPatterns) {
            this.urlPatterns = urlPatterns;
        }

        public String getUrlExcludes() {
            return urlExcludes;
        }

        public void setUrlExcludes(String urlExcludes) {
            this.urlExcludes = urlExcludes;
        }
    }


}
