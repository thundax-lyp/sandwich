package com.github.thundax.modules.auth.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import org.junit.Test;

public class OpenApiSignatureVerifierTest {

    private final OpenApiSignatureVerifier verifier = new OpenApiSignatureVerifier();

    @Test
    public void shouldVerifyBodySha256() {
        String bodyHash = verifier.sha256Hex("{\"title\":\"demo\"}".getBytes(StandardCharsets.UTF_8));

        assertTrue(verifier.matchesBodyHash("{\"title\":\"demo\"}".getBytes(StandardCharsets.UTF_8), bodyHash));
        assertFalse(verifier.matchesBodyHash("{\"title\":\"changed\"}".getBytes(StandardCharsets.UTF_8), bodyHash));
    }

    @Test
    public void shouldVerifyHmacSignatureAgainstCanonicalRequest() {
        OpenApiCanonicalRequest canonicalRequest = new OpenApiCanonicalRequest(
                "POST", "/open-api/api/submission/submission/create", "b=2&a=1", "1778832000000", "nonce-1", "hash");
        String signature = verifier.hmacSha256Hex("swas_demo", canonicalRequest.value());

        assertTrue(verifier.verify("swas_demo", canonicalRequest, signature));
        assertFalse(verifier.verify("swas_changed", canonicalRequest, signature));
    }
}
