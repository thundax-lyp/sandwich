package com.github.thundax.modules.config;

import com.github.thundax.modules.interceptor.InterfaceInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** MVC配置 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private InterfaceInterceptor interfaceInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // addPathPatterns("/**") 表示拦截所有的请求，
        registry.addInterceptor(interfaceInterceptor).addPathPatterns("/consult/app/**");
    }
}
