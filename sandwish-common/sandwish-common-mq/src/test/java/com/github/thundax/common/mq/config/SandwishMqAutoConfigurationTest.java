package com.github.thundax.common.mq.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import com.github.thundax.common.mq.SandwishMqProperties;
import com.github.thundax.common.mq.SandwishMqSender;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class SandwishMqAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(SandwishMqAutoConfiguration.class));

    @Test
    public void shouldCreateNoOpSenderAndProperties() {
        contextRunner.run(context -> {
            assertNotNull(context.getBean(SandwishMqProperties.class));
            assertNotNull(context.getBean(SandwishMqSender.class));
        });
    }

    @Test
    public void shouldBindProperties() {
        contextRunner
                .withPropertyValues("sandwish.mq.enabled=false", "sandwish.mq.default-topic=ops.log")
                .run(context -> {
                    SandwishMqProperties properties = context.getBean(SandwishMqProperties.class);
                    assertFalse(properties.isEnabled());
                    assertEquals("ops.log", properties.getDefaultTopic());
                });
    }

    @Test
    public void shouldKeepCustomSender() {
        contextRunner.withUserConfiguration(CustomSenderConfiguration.class).run(context -> {
            SandwishMqSender sender = context.getBean(SandwishMqSender.class);
            assertSame(CustomSenderConfiguration.SENDER, sender);
        });
    }

    @Configuration
    static class CustomSenderConfiguration {

        private static final SandwishMqSender SENDER = message -> {};

        @Bean
        public SandwishMqSender sandwishMqSender() {
            return SENDER;
        }
    }
}
