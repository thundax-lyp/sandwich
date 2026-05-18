package com.github.thundax.common.web.health;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SandwishHealthProbeControllerTest {

    @Test
    public void shouldReturnUpForLivenessAndReadiness() {
        SandwishHealthProbeController controller = new SandwishHealthProbeController();

        assertEquals("UP", controller.liveness().get("status"));
        assertEquals("UP", controller.readiness().get("status"));
    }
}
