package com.github.thundax.common.log.producer;

import com.github.thundax.common.log.model.SysLogEvent;
import com.github.thundax.common.mq.SandwishMqMessage;
import com.github.thundax.common.mq.SandwishMqProperties;
import com.github.thundax.common.mq.SandwishMqSender;
import com.github.thundax.common.utils.JsonUtils;

public class MqSysLogProducer implements SysLogProducer {

    private static final String SYS_LOG_TAG = "SYS_LOG";

    private final SandwishMqSender sandwishMqSender;
    private final SandwishMqProperties sandwishMqProperties;

    public MqSysLogProducer(SandwishMqSender sandwishMqSender, SandwishMqProperties sandwishMqProperties) {
        this.sandwishMqSender = sandwishMqSender;
        this.sandwishMqProperties = sandwishMqProperties;
    }

    @Override
    public void produce(SysLogEvent event) {
        if (!sandwishMqProperties.isEnabled()) {
            return;
        }
        SandwishMqMessage message = new SandwishMqMessage();
        message.setTopic(sandwishMqProperties.getDefaultTopic());
        message.setTag(SYS_LOG_TAG);
        message.setKey(event.getClassName() + "#" + event.getMethodName());
        message.setPayload(JsonUtils.toJson(event));
        sandwishMqSender.send(message);
    }
}
