package com.github.thundax.common.log.config;

import com.github.thundax.common.log.aspect.SysLogAspect;
import com.github.thundax.common.log.producer.MqSysLogProducer;
import com.github.thundax.common.log.producer.NoOpSysLogProducer;
import com.github.thundax.common.log.producer.SysLogProducer;
import com.github.thundax.common.mq.SandwishMqProperties;
import com.github.thundax.common.mq.SandwishMqSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class SandwishLogAutoConfiguration {

    @Bean
    @ConditionalOnBean(SandwishMqSender.class)
    @ConditionalOnMissingBean(SysLogProducer.class)
    public SysLogProducer mqSysLogProducer(
            SandwishMqSender sandwishMqSender, SandwishMqProperties sandwishMqProperties) {
        return new MqSysLogProducer(sandwishMqSender, sandwishMqProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public SysLogProducer sysLogProducer() {
        return new NoOpSysLogProducer();
    }

    @Bean
    @ConditionalOnMissingBean
    public SysLogAspect sysLogAspect(SysLogProducer sysLogProducer) {
        return new SysLogAspect(sysLogProducer);
    }
}
