package com.github.thundax.common.swagger.configure;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import springfox.documentation.spring.web.plugins.Docket;

public class SwaggerConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(SwaggerConfiguration.class));

    @Test
    public void shouldCreateDocketWithDefaultProperties() {
        contextRunner.run(context -> {
            assertNotNull(context.getBean(SwaggerProperties.class));
            assertNotNull(context.getBean(Docket.class));
        });
    }

    @Test
    public void shouldBindEnabledProperty() {
        contextRunner
                .withPropertyValues("swagger.enabled=false")
                .run(context ->
                        assertFalse(context.getBean(SwaggerProperties.class).isEnabled()));
    }
}
