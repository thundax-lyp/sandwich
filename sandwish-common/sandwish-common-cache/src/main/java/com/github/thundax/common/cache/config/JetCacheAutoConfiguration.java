package com.github.thundax.common.cache.config;

import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableMethodCache(basePackages = "com.github.thundax")
@EnableCreateCacheAnnotation
public class JetCacheAutoConfiguration {

    static final String BASE_PACKAGE = "com.github.thundax";
    static final int DEFAULT_STAT_INTERVAL_MINUTES = 15;

    @Bean
    public static BeanPostProcessor jetCacheGlobalConfigBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof GlobalCacheConfig) {
                    GlobalCacheConfig globalCacheConfig = (GlobalCacheConfig) bean;
                    globalCacheConfig.setHiddenPackages(new String[] {BASE_PACKAGE});
                    globalCacheConfig.setAreaInCacheName(true);
                    globalCacheConfig.setPenetrationProtect(true);
                    if (globalCacheConfig.getStatIntervalMinutes() <= 0) {
                        globalCacheConfig.setStatIntervalMinutes(DEFAULT_STAT_INTERVAL_MINUTES);
                    }
                }
                return bean;
            }
        };
    }
}
