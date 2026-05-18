package com.github.thundax.common.test.integration;

import org.junit.Test;
import org.springframework.mock.env.MockEnvironment;

public class IntegrationTestProfileGuardTest {

    @Test
    public void shouldPassWhenIntegrationTestEnabled() {
        MockEnvironment environment =
                new MockEnvironment().withProperty(IntegrationTestProfileGuard.ENABLED_PROPERTY, "true");

        IntegrationTestProfileGuard.assertEnabled(environment);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailWhenIntegrationTestDisabled() {
        MockEnvironment environment =
                new MockEnvironment().withProperty(IntegrationTestProfileGuard.ENABLED_PROPERTY, "false");

        IntegrationTestProfileGuard.assertEnabled(environment);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailWhenIntegrationTestSwitchMissing() {
        IntegrationTestProfileGuard.assertEnabled(new MockEnvironment());
    }
}
