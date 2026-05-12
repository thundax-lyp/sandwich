package com.github.thundax.common.mq.support;

import com.github.thundax.common.mq.SandwishMqMessage;
import com.github.thundax.common.mq.SandwishMqSender;
import java.util.Map;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class RabbitSandwishMqSender implements SandwishMqSender {

    private final RabbitTemplate rabbitTemplate;

    public RabbitSandwishMqSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void send(SandwishMqMessage message) {
        MessagePostProcessor postProcessor = mqMessage -> {
            for (Map.Entry<String, String> entry : message.getHeaders().entrySet()) {
                mqMessage.getMessageProperties().setHeader(entry.getKey(), entry.getValue());
            }
            return mqMessage;
        };
        if (message.getExchange() == null || message.getExchange().trim().isEmpty()) {
            rabbitTemplate.convertAndSend(message.getRoutingKey(), message.getPayload(), postProcessor);
            return;
        }
        rabbitTemplate.convertAndSend(
                message.getExchange(), message.getRoutingKey(), message.getPayload(), postProcessor);
    }
}
