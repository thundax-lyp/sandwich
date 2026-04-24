package com.github.thundax.autoconfigure.shiro;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * @author wdit
 */
@ConfigurationProperties(prefix = "vltava.shiro")
public class ShiroProperties {

    private Map<String, String> chainDefinition;

    public Map<String, String> getChainDefinition() {
        return chainDefinition;
    }

    public void setChainDefinition(Map<String, String> chainDefinition) {
        this.chainDefinition = chainDefinition;
    }

}
