package com.github.thundax.common.log.producer;

import com.github.thundax.common.log.model.SysLogEvent;

public class NoOpSysLogProducer implements SysLogProducer {

    @Override
    public void produce(SysLogEvent event) {
        // Default producer intentionally discards events when no backend is configured.
    }
}
