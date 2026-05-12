package com.github.thundax.common.mq;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sandwish.mq")
public class SandwishMqProperties {

    private boolean enabled = true;
    private SandwishMqType type = SandwishMqType.RABBITMQ;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public SandwishMqType getType() {
        return type;
    }

    public void setType(SandwishMqType type) {
        this.type = type;
    }
}
