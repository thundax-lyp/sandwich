package com.github.thundax.autoconfigure;

import com.github.thundax.modules.sys.service.SysLogMessageService;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "sandwish.mq", name = "type", havingValue = "RABBITMQ", matchIfMissing = true)
public class RabbitMqConfiguration {

    @Bean
    public Queue saveLogQueue(
            @Value("${sandwish.log.sys.queue:" + SysLogMessageService.QUEUE_SAVE_LOG + "}") String queueName) {
        return new Queue(queueName);
    }
}
