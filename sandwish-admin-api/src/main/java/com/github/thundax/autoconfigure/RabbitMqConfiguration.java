package com.github.thundax.autoconfigure;

import com.github.thundax.modules.sys.service.SysLogMessageService;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfiguration {

    @Bean
    public Queue saveLogQueue() {
        return new Queue(SysLogMessageService.QUEUE_SAVE_LOG);
    }
}
