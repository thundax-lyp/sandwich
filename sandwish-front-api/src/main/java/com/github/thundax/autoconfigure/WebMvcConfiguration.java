package com.github.thundax.autoconfigure;

import com.github.thundax.common.filter.xss.XssFilter;
import com.github.thundax.common.thread.PooledThreadLocalFilter;
import com.github.thundax.common.web.ProcessTimeFilter;
import com.github.thundax.modules.storage.converter.StorageConverter;
import com.github.thundax.modules.storage.servlet.StorageServlet;
import com.github.thundax.modules.sys.dao.SmsValidateCodeDao;
import com.github.thundax.modules.sys.servlet.SmsValidateCodeServlet;
import com.github.thundax.modules.sys.servlet.ValidateCodeServlet;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.DispatcherType;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** @author wdit */
@Configuration
@EnableWebMvc
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Bean
    public ServletRegistrationBean<ValidateCodeServlet> validateCodeServletServletRegistrationBean(
            VltavaProperties properties) {
        ValidateCodeServlet.setWhiteCaptcha(properties.getWhiteCaptcha());

        ServletRegistrationBean<ValidateCodeServlet> bean = new ServletRegistrationBean<>();
        bean.setServlet(new ValidateCodeServlet());
        bean.addUrlMappings("/servlet/validateCodeServlet");
        return bean;
    }

    @Bean
    public ServletRegistrationBean<SmsValidateCodeServlet>
            smsValidateCodeServletServletRegistrationBean(
                    VltavaProperties properties, SmsValidateCodeDao smsValidateCodeDao) {
        SmsValidateCodeServlet.setWhiteCaptcha(properties.getWhiteCaptcha());

        ServletRegistrationBean<SmsValidateCodeServlet> bean = new ServletRegistrationBean<>();
        bean.setServlet(new SmsValidateCodeServlet(smsValidateCodeDao));
        bean.addUrlMappings("/servlet/smsValidateCodeServlet");
        return bean;
    }

    @Bean
    public ServletRegistrationBean<StorageServlet> resourceFileServletServletRegistrationBean(
            VltavaProperties properties, StorageConverter converter) {
        ServletRegistrationBean<StorageServlet> bean = new ServletRegistrationBean<>();
        bean.setServlet(new StorageServlet(converter));
        VltavaProperties.UploadProperties upload = properties.getUpload();
        bean.addUrlMappings(upload.getServletPath() + "*");
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

    @Bean
    public FilterRegistrationBean<XssFilter> xssFilterRegistrationBean(
            VltavaProperties properties) {
        FilterRegistrationBean<XssFilter> registration = new FilterRegistrationBean<>();
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.setFilter(new XssFilter());
        registration.addUrlPatterns(properties.getXssFilter().getUrlPatterns().split(","));
        registration.setName("XssFilter");
        registration.setOrder(Integer.MAX_VALUE);
        Map<String, String> initParameters = new HashMap<>();
        initParameters.put("tagExcludes", properties.getXssFilter().getTagExcludes());
        initParameters.put("tagIncludes", properties.getXssFilter().getTagIncludes());
        initParameters.put("urlExcludes", properties.getXssFilter().getUrlExcludes());
        initParameters.put("enabled", properties.getXssFilter().getEnabled());
        registration.setInitParameters(initParameters);
        return registration;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");

        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/favicon.ico");
    }
}
