package com.github.thundax.common.mq.configure;

import com.github.thundax.common.mq.SandwishMqType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "sandwish.mq")
public class SandwishMqProperties {

    private boolean enabled = true;
    private SandwishMqType type = SandwishMqType.RABBITMQ;
}
