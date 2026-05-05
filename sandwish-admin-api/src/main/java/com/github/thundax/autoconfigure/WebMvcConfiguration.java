package com.github.thundax.autoconfigure;

import com.github.thundax.common.thread.PooledThreadLocalFilter;
import com.github.thundax.common.web.ProcessTimeFilter;
import com.github.thundax.modules.auth.filter.ResponseWrapperFilter;
import com.github.thundax.modules.storage.store.LocalFileStoredObjectStore;
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
    public StoredObjectStore storedObjectStore(VltavaProperties properties) {
        VltavaProperties.UploadProperties upload = properties.getUpload();
        return new LocalFileStoredObjectStore(upload.getStoragePath(), upload.getContentPath());
    }

    @Bean
    public FilterRegistrationBean<ResponseWrapperFilter> responseWrapperFilter(VltavaProperties properties) {
        VltavaProperties.ResponseWrapperFilterProperties wrapperFilterProperties =
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
