package com.github.thundax.integration.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.github.thundax.integration.AbstractOpenApiIT;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

public class OpenApiSignatureIT extends AbstractOpenApiIT {

    private static final String PAGE_PATH = "/api/submission/submission/page";

    @Before
    public void prepareData() {
        prepareIntegrationData();
    }

    @Test
    public void shouldAcceptSignedOpenApiRequest() {
        Map<String, Object> page = dataMap(postSignedJson(PAGE_PATH, pageRequest(), Map.class));

        assertTrue(((Number) page.get("count")).intValue() >= 1);
    }

    @Test
    public void shouldRejectInvalidSignature() {
        Map<String, Object> request = pageRequest();
        Map<String, String> headers = signedHeaders("POST", PAGE_PATH, "", json(request), "it-open-invalid-signature");
        headers.put("X-Sandwish-Signature", "invalid-signature");

        assertUnauthorized(request, headers);
    }

    @Test
    public void shouldRejectReplayedNonce() {
        Map<String, Object> request = pageRequest();
        Map<String, String> headers = signedHeaders("POST", PAGE_PATH, "", json(request), "it-open-replayed-nonce");
        postSignedJson(PAGE_PATH, request, headers, Map.class);

        assertUnauthorized(request, headers);
    }

    @Test
    public void shouldRejectIpOutsideWhitelist() {
        Map<String, Object> request = pageRequest();
        Map<String, String> headers = signedHeaders("POST", PAGE_PATH, "", json(request), "it-open-ip-not-allowed");
        headers.put("X-Forwarded-For", "127.0.0.2");

        try {
            postSignedJson(PAGE_PATH, request, headers, Map.class);
        } catch (HttpClientErrorException expected) {
            assertEquals(HttpStatus.FORBIDDEN, expected.getStatusCode());
            return;
        }
        fail("open api should reject requests outside client IP whitelist");
    }

    private void assertUnauthorized(Map<String, Object> request, Map<String, String> headers) {
        try {
            postSignedJson(PAGE_PATH, request, headers, Map.class);
        } catch (HttpClientErrorException expected) {
            assertEquals(HttpStatus.UNAUTHORIZED, expected.getStatusCode());
            return;
        }
        fail("open api should reject unauthorized request");
    }

    private Map<String, Object> pageRequest() {
        Map<String, Object> request = request("pageNo", 1);
        request.put("pageSize", 5);
        return request;
    }
}
