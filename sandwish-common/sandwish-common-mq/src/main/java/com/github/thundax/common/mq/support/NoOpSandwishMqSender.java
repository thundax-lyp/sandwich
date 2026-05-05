package com.github.thundax.common.mq.support;

import com.github.thundax.common.mq.SandwishMqMessage;
import com.github.thundax.common.mq.SandwishMqSender;

public class NoOpSandwishMqSender implements SandwishMqSender {

    @Override
    public void send(SandwishMqMessage message) {
        // Default sender intentionally discards messages when no broker adapter is configured.
    }
}
