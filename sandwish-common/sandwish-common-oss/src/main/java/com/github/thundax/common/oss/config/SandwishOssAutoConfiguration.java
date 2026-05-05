package com.github.thundax.common.oss.config;

import com.github.thundax.common.oss.client.ObjectStorageClient;
import com.github.thundax.common.oss.support.LocalFileObjectStorageClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SandwishOssProperties.class)
public class SandwishOssAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "sandwish.oss", name = "type", havingValue = "local", matchIfMissing = true)
    public ObjectStorageClient localFileObjectStorageClient(SandwishOssProperties properties) {
        return new LocalFileObjectStorageClient(
                properties.getLocal().getRootPath(), properties.getLocal().getLocationPrefix());
    }
}
