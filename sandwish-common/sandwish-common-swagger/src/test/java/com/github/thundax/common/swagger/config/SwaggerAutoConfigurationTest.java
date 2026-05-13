package com.github.thundax.common.swagger.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.HttpStatus;
import springfox.documentation.spring.web.plugins.Docket;

public class SwaggerAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(SwaggerAutoConfiguration.class));

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

    @Test
    public void shouldHandleSwaggerUiCsrfProbe() {
        contextRunner.run(context -> {
            SwaggerAutoConfiguration.SwaggerCsrfProbeController controller =
                    context.getBean(SwaggerAutoConfiguration.SwaggerCsrfProbeController.class);

            assertEquals(HttpStatus.NO_CONTENT, controller.csrf().getStatusCode());
        });
    }
}
