package com.github.thundax;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class OpenApiApplicationTest {

    @Test
    public void shouldExposeOpenApiApplicationClass() {
        assertEquals("OpenApiApplication", OpenApiApplication.class.getSimpleName());
    }
}
