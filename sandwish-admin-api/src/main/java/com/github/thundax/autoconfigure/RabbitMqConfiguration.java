package com.github.thundax.autoconfigure;

import com.github.thundax.modules.sys.utils.SysLogUtils;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** @author thundax */
@Configuration
public class RabbitMqConfiguration {

    @Bean
    public Queue saveLogQueue() {
        return new Queue(SysLogUtils.QUEUE_SAVE_LOG);
    }
}
