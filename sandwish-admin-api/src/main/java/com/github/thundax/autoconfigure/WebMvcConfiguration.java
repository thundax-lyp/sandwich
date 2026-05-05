package com.github.thundax.autoconfigure;

import com.github.thundax.common.oss.client.ObjectStorageClient;
import com.github.thundax.common.oss.config.SandwishOssProperties;
import com.github.thundax.common.oss.support.LocalFileObjectStorageClient;
import com.github.thundax.common.thread.PooledThreadLocalFilter;
import com.github.thundax.common.web.ProcessTimeFilter;
import com.github.thundax.modules.auth.filter.ResponseWrapperFilter;
import com.github.thundax.modules.storage.entity.enums.StorageType;
import com.github.thundax.modules.storage.store.ObjectStorageStoredObjectStore;
import com.github.thundax.modules.storage.store.StoredObjectStore;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Bean
    public StoredObjectStore storedObjectStore(
            SandwishProperties properties,
            ObjectStorageClient objectStorageClient,
            SandwishOssProperties ossProperties) {
        SandwishProperties.UploadProperties upload = properties.getUpload();
        if (StorageType.OSS == storageType(ossProperties)) {
            return new ObjectStorageStoredObjectStore(
                    objectStorageClient, StorageType.OSS, ossProperties.getS3().getBucket(), upload.getContentPath());
        }
        return new ObjectStorageStoredObjectStore(
                new LocalFileObjectStorageClient(upload.getStoragePath(), ""),
                StorageType.LOCAL_FILE,
                upload.getStoragePath(),
                upload.getContentPath());
    }

    private StorageType storageType(SandwishOssProperties ossProperties) {
        return "s3".equalsIgnoreCase(ossProperties.getType()) ? StorageType.OSS : StorageType.LOCAL_FILE;
    }

    @Bean
    public FilterRegistrationBean<ResponseWrapperFilter> responseWrapperFilter(SandwishProperties properties) {
        SandwishProperties.ResponseWrapperFilterProperties wrapperFilterProperties =
                properties.getResponseWrapperFilter();

        FilterRegistrationBean<ResponseWrapperFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new ResponseWrapperFilter(wrapperFilterProperties));

        if (wrapperFilterProperties.getUrlPatterns() != null
                && !wrapperFilterProperties.getUrlPatterns().isEmpty()) {
            bean.setUrlPatterns(wrapperFilterProperties.getUrlPatterns());
        } else {
            bean.addUrlPatterns("/api/*");
        }

        return bean;
    }

    @Bean
    public FilterRegistrationBean<PooledThreadLocalFilter> polledThreadFilterRegistrationBean() {
        FilterRegistrationBean<PooledThreadLocalFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new PooledThreadLocalFilter());
        bean.addUrlPatterns("/*");
        return bean;
    }

    @Bean
    public FilterRegistrationBean<ProcessTimeFilter> processTimeFilterRegistrationBean() {
        FilterRegistrationBean<ProcessTimeFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new ProcessTimeFilter());
        bean.addUrlPatterns("/admin/*");
        return bean;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");

        registry.addResourceHandler("/favicon.ico").addResourceLocations("classpath:/static/favicon.ico");

        registry.addResourceHandler("/testcase/**").addResourceLocations("classpath:/testcase/");
    }
}
