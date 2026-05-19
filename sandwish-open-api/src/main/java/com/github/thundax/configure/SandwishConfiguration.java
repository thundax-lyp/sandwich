package com.github.thundax.configure;

import com.github.thundax.common.jasypt.JasyptStringEncryptor;
import com.github.thundax.modules.auth.configure.CaptchaWhitelistProperties;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({SandwishProperties.class, CaptchaWhitelistProperties.class})
public class SandwishConfiguration {

    @Bean(name = "jasyptStringEncryptor")
    public StringEncryptor stringEncryptor() {
        return new JasyptStringEncryptor();
    }
}
