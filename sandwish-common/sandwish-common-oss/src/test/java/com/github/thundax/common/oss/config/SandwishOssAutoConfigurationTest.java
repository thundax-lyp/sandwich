package com.github.thundax.common.oss.config;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

public class SandwishOssAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(SandwishOssAutoConfiguration.class));

    @Test
    public void shouldCreateLocalObjectStorageClientByDefault() {
        contextRunner.run(context -> assertTrue(context.containsBean("localFileObjectStorageClient")));
    }

    @Test
    public void shouldRejectS3WhenRequiredConfigurationMissing() {
        contextRunner
                .withPropertyValues("sandwish.oss.type=s3")
                .run(context -> assertTrue(hasCause(context.getStartupFailure(), IllegalStateException.class)));
    }

    @Test
    public void shouldRejectUnsupportedOssType() {
        contextRunner
                .withPropertyValues("sandwish.oss.type=ftp")
                .run(context -> assertTrue(hasCause(context.getStartupFailure(), IllegalStateException.class)));
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
