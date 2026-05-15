package com.github.thundax.modules.auth.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class OpenApiIpWhitelistMatcherTest {

    private final OpenApiIpWhitelistMatcher matcher = new OpenApiIpWhitelistMatcher(new ObjectMapper());

    @Test
    public void shouldAllowBlankWhitelist() {
        assertTrue(matcher.matches(null, "127.0.0.1"));
        assertTrue(matcher.matches("[]", "127.0.0.1"));
    }

    @Test
    public void shouldMatchSingleIpAndCidr() {
        assertTrue(matcher.matches("[\"127.0.0.1\"]", "127.0.0.1"));
        assertFalse(matcher.matches("[\"127.0.0.1\"]", "127.0.0.2"));
        assertTrue(matcher.matches("[\"10.0.0.0/24\"]", "10.0.0.7"));
        assertFalse(matcher.matches("[\"10.0.0.0/24\"]", "10.0.1.7"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectInvalidWhitelistJson() {
        matcher.matches("not-json", "127.0.0.1");
    }
}
