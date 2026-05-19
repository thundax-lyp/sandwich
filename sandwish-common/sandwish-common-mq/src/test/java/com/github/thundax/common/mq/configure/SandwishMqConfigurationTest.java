package com.github.thundax.common.mq.configure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.github.thundax.common.mq.SandwishMqSender;
import com.github.thundax.common.mq.SandwishMqType;
import com.github.thundax.common.mq.support.NoOpSandwishMqSender;
import com.github.thundax.common.mq.support.RabbitSandwishMqSender;
import com.github.thundax.common.mq.support.RocketMqSandwishMqSender;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class SandwishMqConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(SandwishMqConfiguration.class));

    @Test
    public void shouldRejectEnabledMqWhenNoBrokerClientConfigured() {
        contextRunner.run(context -> assertTrue(hasCause(context.getStartupFailure(), IllegalStateException.class)));
    }

    @Test
    public void shouldCreatePropertiesWithoutSenderWhenDisabledAndNoBrokerClientConfigured() {
        contextRunner.withPropertyValues("sandwish.mq.enabled=false").run(context -> {
            assertNotNull(context.getBean(SandwishMqProperties.class));
            assertTrue(context.getBean(SandwishMqSender.class) instanceof NoOpSandwishMqSender);
        });
    }

    @Test
    public void shouldBindProperties() {
        contextRunner
                .withPropertyValues("sandwish.mq.enabled=false", "sandwish.mq.type=ROCKETMQ")
                .run(context -> {
                    SandwishMqProperties properties = context.getBean(SandwishMqProperties.class);
                    assertFalse(properties.isEnabled());
                    assertEquals(SandwishMqType.ROCKETMQ, properties.getType());
                });
    }

    @Test
    public void shouldCreateNoOpSenderWhenDisabled() {
        contextRunner.withPropertyValues("sandwish.mq.enabled=false").run(context -> {
            SandwishMqSender sender = context.getBean(SandwishMqSender.class);
            assertTrue(sender instanceof NoOpSandwishMqSender);
        });
    }

    @Test
    public void shouldCreateRabbitSenderWhenRabbitTypeConfigured() {
        contextRunner
                .withUserConfiguration(RabbitTemplateConfiguration.class)
                .withPropertyValues("sandwish.mq.type=RABBITMQ")
                .run(context -> {
                    SandwishMqSender sender = context.getBean(SandwishMqSender.class);
                    assertTrue(sender instanceof RabbitSandwishMqSender);
                });
    }

    @Test
    public void shouldCreateRocketSenderWhenRocketTypeConfigured() {
        contextRunner
                .withUserConfiguration(RocketMqTemplateConfiguration.class)
                .withPropertyValues("sandwish.mq.type=ROCKETMQ", "rocketmq.name-server=127.0.0.1:9876")
                .run(context -> {
                    SandwishMqSender sender = context.getBean(SandwishMqSender.class);
                    assertTrue(sender instanceof RocketMqSandwishMqSender);
                });
    }

    @Test
    public void shouldRejectRocketMqWhenNameServerMissing() {
        contextRunner
                .withUserConfiguration(RocketMqTemplateConfiguration.class)
                .withPropertyValues("sandwish.mq.type=ROCKETMQ")
                .run(context -> assertTrue(hasCause(context.getStartupFailure(), IllegalStateException.class)));
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

    @Configuration
    static class RabbitTemplateConfiguration {

        @Bean
        public RabbitTemplate rabbitTemplate() {
            return mock(RabbitTemplate.class);
        }
    }

    @Configuration
    static class RocketMqTemplateConfiguration {

        @Bean
        public RocketMQTemplate rocketMQTemplate() {
            return mock(RocketMQTemplate.class);
        }
    }

    private boolean hasCause(Throwable throwable, Class<? extends Throwable> causeType) {
        Throwable cause = throwable;
        while (cause != null) {
            if (causeType.isInstance(cause)) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
