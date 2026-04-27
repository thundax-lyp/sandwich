package com.github.thundax.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** @Auther: zhangrudong @Date: 2021/10/20 15:19 @Description: */
@Component
@ConfigurationProperties(prefix = "vltava.web")
public class WebProperties {

    private String baseUrl;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
