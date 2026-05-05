package com.github.thundax.common.log.producer;

import static org.junit.Assert.assertEquals;

import com.github.thundax.common.log.model.SysLogEvent;
import com.github.thundax.common.mq.SandwishMqMessage;
import com.github.thundax.common.mq.SandwishMqProperties;
import com.github.thundax.common.mq.SandwishMqSender;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class MqSysLogProducerTest {

    @Test
    public void shouldSendSysLogMessage() {
        RecordingSender sender = new RecordingSender();
        SandwishMqProperties properties = new SandwishMqProperties();
        properties.setDefaultTopic("ops.log");
        MqSysLogProducer producer = new MqSysLogProducer(sender, properties);
        SysLogEvent event = event();

        producer.produce(event);

        assertEquals(1, sender.messages.size());
        SandwishMqMessage message = sender.messages.get(0);
        assertEquals("ops.log", message.getTopic());
        assertEquals("SYS_LOG", message.getTag());
        assertEquals("ExampleService#create", message.getKey());
    }

    @Test
    public void shouldSkipWhenMqDisabled() {
        RecordingSender sender = new RecordingSender();
        SandwishMqProperties properties = new SandwishMqProperties();
        properties.setEnabled(false);
        MqSysLogProducer producer = new MqSysLogProducer(sender, properties);

        producer.produce(event());

        assertEquals(0, sender.messages.size());
    }

    private SysLogEvent event() {
        SysLogEvent event = new SysLogEvent();
        event.setClassName("ExampleService");
        event.setMethodName("create");
        event.setAction("create");
        return event;
    }

    private static class RecordingSender implements SandwishMqSender {

        private final List<SandwishMqMessage> messages = new ArrayList<>();

        @Override
        public void send(SandwishMqMessage message) {
            messages.add(message);
        }
    }
}
