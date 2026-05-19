package com.github.thundax.configure;

import com.github.thundax.common.oss.client.ObjectStorageClient;
import com.github.thundax.common.oss.configure.SandwishOssProperties;
import com.github.thundax.common.web.ProcessTimeFilter;
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
        if (isS3(ossProperties)) {
            return new ObjectStorageStoredObjectStore(
                    objectStorageClient, ossProperties.getS3().getBucket(), upload.getContentPath());
        }
        return new ObjectStorageStoredObjectStore(
                objectStorageClient, ossProperties.getLocal().getRootPath(), upload.getContentPath());
    }

    private boolean isS3(SandwishOssProperties ossProperties) {
        return "s3".equalsIgnoreCase(ossProperties.getType());
    }

    @Bean
    public FilterRegistrationBean<ProcessTimeFilter> processTimeFilterRegistrationBean() {
        FilterRegistrationBean<ProcessTimeFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new ProcessTimeFilter());
        bean.addUrlPatterns("/open/*");
        return bean;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/favicon.ico").addResourceLocations("classpath:/static/favicon.ico");
    }
}
