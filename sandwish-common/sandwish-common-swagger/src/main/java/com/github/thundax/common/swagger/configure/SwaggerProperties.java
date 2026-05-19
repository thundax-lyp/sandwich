package com.github.thundax.common.swagger.configure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "swagger")
public class SwaggerProperties {

    private boolean enabled = true;
    private String basePackage = "com.github.thundax";
    private String title = "Sandwish API";
    private String description = "";
    private String termsOfServiceUrl = "";
    private String contactName = "support";
    private String contactUrl = "";
    private String contactEmail = "";
    private String license = "";
    private String licenseUrl = "";
    private String version = "1.0.0";
}
