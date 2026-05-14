package com.github.thundax.autoconfigure;

import com.github.thundax.common.oss.client.ObjectStorageClient;
import com.github.thundax.common.oss.config.SandwishOssProperties;
import com.github.thundax.common.web.ProcessTimeFilter;
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
                objectStorageClient,
                StorageType.LOCAL_FILE,
                ossProperties.getLocal().getRootPath(),
                upload.getContentPath());
    }

    private StorageType storageType(SandwishOssProperties ossProperties) {
        return "s3".equalsIgnoreCase(ossProperties.getType()) ? StorageType.OSS : StorageType.LOCAL_FILE;
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
