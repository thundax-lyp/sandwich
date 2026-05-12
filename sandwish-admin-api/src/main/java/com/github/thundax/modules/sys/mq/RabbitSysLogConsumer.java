package com.github.thundax.modules.sys.mq;

import com.github.thundax.modules.sys.service.SysLogMessageService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "sandwish.mq", name = "type", havingValue = "RABBITMQ", matchIfMissing = true)
public class RabbitSysLogConsumer {

    private final SysLogMessageService sysLogMessageService;

    public RabbitSysLogConsumer(SysLogMessageService sysLogMessageService) {
        this.sysLogMessageService = sysLogMessageService;
    }

    @RabbitListener(
            queues = "${sandwish.log.sys.queue:" + SysLogMessageService.QUEUE_SAVE_LOG + "}",
            concurrency = "${sandwish.log.sys.rabbit.concurrency:2}")
    public void onMessage(@Payload String payload) {
        sysLogMessageService.consumeLog(payload);
    }
}
