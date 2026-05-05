package com.github.thundax.common.mq.config;

import com.github.thundax.common.mq.SandwishMqProperties;
import com.github.thundax.common.mq.SandwishMqSender;
import com.github.thundax.common.mq.support.NoOpSandwishMqSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SandwishMqProperties.class)
public class SandwishMqAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SandwishMqSender sandwishMqSender() {
        return new NoOpSandwishMqSender();
    }
}
