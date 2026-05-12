package com.github.thundax.common.mq.config;

import com.github.thundax.common.mq.SandwishMqProperties;
import com.github.thundax.common.mq.SandwishMqSender;
import com.github.thundax.common.mq.support.NoOpSandwishMqSender;
import com.github.thundax.common.mq.support.RabbitSandwishMqSender;
import com.github.thundax.common.mq.support.RocketMqSandwishMqSender;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(
        name = {
            "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration",
            "org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration"
        })
@EnableConfigurationProperties(SandwishMqProperties.class)
public class SandwishMqAutoConfiguration {

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
}
