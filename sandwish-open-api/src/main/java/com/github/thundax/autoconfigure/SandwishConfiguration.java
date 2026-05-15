package com.github.thundax.autoconfigure;

import com.github.thundax.common.jasypt.JasyptStringEncryptor;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SandwishProperties.class)
public class SandwishConfiguration {

    @Bean(name = "jasyptStringEncryptor")
    public StringEncryptor stringEncryptor() {
        return new JasyptStringEncryptor();
    }
}
