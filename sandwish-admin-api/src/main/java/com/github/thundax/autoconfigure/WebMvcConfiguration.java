package com.github.thundax.autoconfigure;

import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.common.thread.PooledThreadLocalFilter;
import com.github.thundax.common.web.ProcessTimeFilter;
import com.github.thundax.modules.auth.filter.AccessTokenFilter;
import com.github.thundax.modules.auth.filter.ResponseWrapperFilter;
import com.github.thundax.modules.auth.service.AuthService;
import com.github.thundax.modules.storage.converter.StorageConverter;
import com.github.thundax.modules.storage.servlet.StorageServlet;
import com.github.thundax.modules.sys.servlet.ValidateCodeServlet;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author thundax
 */
@Configuration
@EnableWebMvc
public class WebMvcConfiguration implements WebMvcConfigurer {


    @Bean
    public ServletRegistrationBean<StorageServlet> resourceFileServletServletRegistrationBean(VltavaProperties properties, StorageConverter converter) {
        ServletRegistrationBean<StorageServlet> bean = new ServletRegistrationBean<>();
        bean.setServlet(new StorageServlet(converter));
        VltavaProperties.UploadProperties upload = properties.getUpload();
        bean.addUrlMappings(upload.getServletPath() + "*");
        return bean;
    }

    @Bean
    public ServletRegistrationBean<ValidateCodeServlet> validateCodeServletServletRegistrationBean(VltavaProperties properties) {
        ValidateCodeServlet.setWhiteCaptcha(properties.getWhiteCaptcha());

        ServletRegistrationBean<ValidateCodeServlet> bean = new ServletRegistrationBean<>();
        bean.setServlet(new ValidateCodeServlet());
        bean.addUrlMappings("/servlet/validateCodeServlet");
        return bean;
    }


//    @Bean
//    public ServletRegistrationBean<StorageServlet> storageServletServletRegistrationBean(VltavaProperties properties) {
//        VltavaProperties.UploadProperties upload = properties.getUpload();
//
//        ServletRegistrationBean<StorageServlet> bean = new ServletRegistrationBean<>();
//        bean.setServlet(new StorageServlet(upload.getStoragePath()));
//        bean.addUrlMappings(upload.getServletPath() + "*");
//
//        return bean;
//    }


    @Bean
    public FilterRegistrationBean<ResponseWrapperFilter> responseWrapperFilter(VltavaProperties properties) {
        VltavaProperties.ResponseWrapperFilterProperties wrapperFilterProperties = properties.getResponseWrapperFilter();

        FilterRegistrationBean<ResponseWrapperFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new ResponseWrapperFilter(wrapperFilterProperties));

        if (ListUtils.isNotEmpty(wrapperFilterProperties.getUrlPatterns())) {
            bean.setUrlPatterns(wrapperFilterProperties.getUrlPatterns());
        } else {
            bean.addUrlPatterns("/api/*");
        }

        return bean;
    }

    @Bean
    public FilterRegistrationBean<AccessTokenFilter> accessTokenFilter(VltavaProperties properties,
                                                                       AuthService authService) {
        VltavaProperties.AccessTokenFilterProperties tokenFilterProperties = properties.getAccessTokenFilter();

        FilterRegistrationBean<AccessTokenFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new AccessTokenFilter(tokenFilterProperties, authService));

        if (ListUtils.isNotEmpty(tokenFilterProperties.getUrlPatterns())) {
            bean.setUrlPatterns(tokenFilterProperties.getUrlPatterns());
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
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");

        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/favicon.ico");

        registry.addResourceHandler("/testcase/**")
                .addResourceLocations("classpath:/testcase/");

        registry.addResourceHandler("swagger-ui.html").addResourceLocations(
                "classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations(
                "classpath:/META-INF/resources/webjars/");

    }

}
