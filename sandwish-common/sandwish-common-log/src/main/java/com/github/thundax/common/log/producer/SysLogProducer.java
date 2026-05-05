package com.github.thundax.common.log.producer;

import com.github.thundax.common.log.model.SysLogEvent;

public interface SysLogProducer {

    void produce(SysLogEvent event);
}
