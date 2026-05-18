package com.github.thundax.integration.open;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.github.thundax.common.test.integration.IntegrationAuthClient.AuthToken;
import com.github.thundax.integration.AbstractAdminApiIT;
import java.util.Arrays;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

public class AdminOpenClientMutationIT extends AbstractAdminApiIT {

    private AuthToken token;

    @Before
    public void prepareData() {
        prepareIntegrationData();
        token = loginAdmin();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldCreateUpdateChangeStatusAndResetSecret() {
        Map<String, Object> created = dataMap(httpClient.postJson(
                "/api/open/client/create",
                clientSaveRequest("Integration Mutable Open Client"),
                authHeaders(token.getToken()),
                Map.class));
        String clientId = String.valueOf(created.get("id"));
        String apiSecret = String.valueOf(created.get("apiSecret"));
        assertNotNull(clientId);
        assertTrue(String.valueOf(created.get("apiKey")).startsWith("swak_"));
        assertTrue(apiSecret.startsWith("swas_"));

        Map<String, Object> updateRequest = clientSaveRequest("Integration Mutable Open Client Updated");
        updateRequest.put("id", clientId);
        Map<String, Object> updated = dataMap(httpClient.postJson(
                "/api/open/client/update", updateRequest, authHeaders(token.getToken()), Map.class));
        assertEquals("Integration Mutable Open Client Updated", updated.get("name"));
        assertEquals("Integration open client mutation", updated.get("remarks"));
        assertTrue(((java.util.List<String>) updated.get("permissions")).contains("submission:submission:page"));

        assertEquals(
                Boolean.TRUE,
                data(httpClient.postJson(
                        "/api/open/client/change-status",
                        statusRequest(clientId, "DISABLED"),
                        authHeaders(token.getToken()),
                        Map.class)));
        Map<String, Object> disabled = dataMap(httpClient.postJson(
                "/api/open/client/get", request("id", clientId), authHeaders(token.getToken()), Map.class));
        assertEquals("DISABLED", disabled.get("status"));

        Map<String, Object> reset = dataMap(httpClient.postJson(
                "/api/open/client/secret/reset", request("id", clientId), authHeaders(token.getToken()), Map.class));
        assertEquals(clientId, String.valueOf(reset.get("id")));
        assertEquals(disabled.get("apiKey"), reset.get("apiKey"));
        assertNotEquals(apiSecret, reset.get("apiSecret"));
        assertTrue(String.valueOf(reset.get("apiSecret")).startsWith("swas_"));
    }

    @Test
    public void shouldRejectReadonlyUserWhenEditingOpenClient() {
        AuthToken readonlyToken = loginAdmin("it-user", "Q1w2e3r$", "8888");
        try {
            httpClient.postJson(
                    "/api/open/client/create",
                    clientSaveRequest("Integration Forbidden Open Client"),
                    authHeaders(readonlyToken.getToken()),
                    Map.class);
        } catch (HttpClientErrorException expected) {
            assertEquals(HttpStatus.FORBIDDEN, expected.getStatusCode());
            return;
        }
        fail("readonly user should not create open client");
    }

    private Map<String, Object> clientSaveRequest(String name) {
        Map<String, Object> request = request("name", name);
        request.put("ipWhitelist", "[\"127.0.0.1\"]");
        request.put("remarks", "Integration open client mutation");
        request.put("permissions", Arrays.asList("submission:submission:create", "submission:submission:page"));
        return request;
    }

    private Map<String, Object> statusRequest(String id, String status) {
        Map<String, Object> request = request("id", id);
        request.put("status", status);
        return request;
    }
}
