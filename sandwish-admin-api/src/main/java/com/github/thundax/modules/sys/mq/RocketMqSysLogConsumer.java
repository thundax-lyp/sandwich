package com.github.thundax.modules.sys.mq;

import com.github.thundax.modules.sys.service.SysLogMessageService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "sandwish.mq", name = "type", havingValue = "ROCKETMQ")
@RocketMQMessageListener(
        topic = "${sandwish.log.sys.topic:" + SysLogMessageService.TOPIC_SAVE_LOG + "}",
        consumerGroup = "${sandwish.log.sys.consumer-group:sandwish-admin-api-sys-log-consumer}",
        selectorExpression = "*")
public class RocketMqSysLogConsumer implements RocketMQListener<String> {

    private final SysLogMessageService sysLogMessageService;

    public RocketMqSysLogConsumer(SysLogMessageService sysLogMessageService) {
        this.sysLogMessageService = sysLogMessageService;
    }

    @Override
    public void onMessage(String payload) {
        sysLogMessageService.consumeLog(payload);
    }
}
