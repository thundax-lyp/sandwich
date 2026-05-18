package com.github.thundax.common.mq.config;

import com.github.thundax.common.mq.SandwishMqProperties;
import com.github.thundax.common.mq.SandwishMqSender;
import com.github.thundax.common.mq.support.NoOpSandwishMqSender;
import com.github.thundax.common.mq.support.RabbitSandwishMqSender;
import com.github.thundax.common.mq.support.RocketMqSandwishMqSender;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

@Configuration
@AutoConfigureAfter(
        name = {
            "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration",
            "org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration"
        })
@EnableConfigurationProperties(SandwishMqProperties.class)
public class SandwishMqAutoConfiguration {

    @Bean
    public SandwishMqConfigurationValidator sandwishMqConfigurationValidator(
            SandwishMqProperties properties, ObjectProvider<SandwishMqSender> senderProvider, Environment environment) {
        return new SandwishMqConfigurationValidator(properties, senderProvider, environment);
    }

    @Bean
    @ConditionalOnProperty(prefix = "sandwish.mq", name = "enabled", havingValue = "false")
    @ConditionalOnMissingBean(SandwishMqSender.class)
    public SandwishMqSender disabledSandwishMqSender() {
        return new NoOpSandwishMqSender();
    }

    @Bean
    @ConditionalOnClass(RabbitTemplate.class)
    @ConditionalOnBean(RabbitTemplate.class)
    @ConditionalOnProperty(prefix = "sandwish.mq", name = "type", havingValue = "RABBITMQ", matchIfMissing = true)
    @ConditionalOnMissingBean(SandwishMqSender.class)
    public SandwishMqSender rabbitMqSender(RabbitTemplate rabbitTemplate) {
        return new RabbitSandwishMqSender(rabbitTemplate);
    }

    @Bean
    @ConditionalOnClass(RocketMQTemplate.class)
    @ConditionalOnBean(RocketMQTemplate.class)
    @ConditionalOnProperty(prefix = "sandwish.mq", name = "type", havingValue = "ROCKETMQ")
    @ConditionalOnMissingBean(SandwishMqSender.class)
    public SandwishMqSender rocketMqSender(RocketMQTemplate rocketMQTemplate) {
        return new RocketMqSandwishMqSender(rocketMQTemplate);
    }

    public static class SandwishMqConfigurationValidator implements InitializingBean {

        private final SandwishMqProperties properties;
        private final ObjectProvider<SandwishMqSender> senderProvider;
        private final Environment environment;

        public SandwishMqConfigurationValidator(
                SandwishMqProperties properties,
                ObjectProvider<SandwishMqSender> senderProvider,
                Environment environment) {
            this.properties = properties;
            this.senderProvider = senderProvider;
            this.environment = environment;
        }

        @Override
        public void afterPropertiesSet() {
            if (!properties.isEnabled()) {
                return;
            }
            if (properties.getType() == null) {
                throw new IllegalStateException("Missing MQ configuration. Configure sandwish.mq.type.");
            }
            switch (properties.getType()) {
                case RABBITMQ:
                    validateSender("RabbitMQ");
                    return;
                case ROCKETMQ:
                    requireText(environment.getProperty("rocketmq.name-server"), "rocketmq.name-server");
                    validateSender("RocketMQ");
                    return;
                default:
                    throw new IllegalStateException("Unsupported MQ type: " + properties.getType());
            }
        }

        private void validateSender(String name) {
            if (senderProvider.getIfAvailable() == null) {
                throw new IllegalStateException(
                        name + " is enabled but no SandwishMqSender was created. Check broker client configuration.");
            }
        }

        private void requireText(String value, String propertyName) {
            if (!StringUtils.hasText(value)) {
                throw new IllegalStateException("Missing RocketMQ configuration. Configure " + propertyName + ".");
            }
        }
    }
}
