package com.github.thundax.integration.open;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.github.thundax.common.test.integration.IntegrationAuthClient.AuthToken;
import com.github.thundax.integration.AbstractAdminApiIT;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class AdminOpenClientQueryIT extends AbstractAdminApiIT {

    private static final String OPEN_CLIENT_ID = "9100000000000050001";

    private AuthToken token;

    @Before
    public void prepareData() {
        prepareIntegrationData();
        token = loginAdmin();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldGetAndPageOpenClients() {
        Map<String, Object> client = dataMap(httpClient.postJson(
                "/api/open/client/get", request("id", OPEN_CLIENT_ID), authHeaders(token.getToken()), Map.class));
        assertEquals("Integration Open Client", client.get("name"));
        assertEquals("ENABLED", client.get("status"));
        assertEquals("it-open-client", client.get("apiKey"));
        assertTrue(((List<String>) client.get("permissions")).contains("submission:submission:create"));

        Map<String, Object> pageRequest = request("pageNo", 1);
        pageRequest.put("pageSize", 10);
        pageRequest.put("name", "Integration Open Client");
        pageRequest.put("status", "ENABLED");
        Map<String, Object> page = dataMap(
                httpClient.postJson("/api/open/client/page", pageRequest, authHeaders(token.getToken()), Map.class));
        assertTrue(((Number) page.get("count")).longValue() >= 1L);
        assertTrue(containsById((List<Map<String, Object>>) page.get("records"), OPEN_CLIENT_ID));
    }

    @Test
    public void shouldAllowReadonlyUserWhenViewingOpenClients() {
        AuthToken readonlyToken = loginAdmin("it-user", "Q1w2e3r$", "8888");
        Map<String, Object> page = dataMap(httpClient.postJson(
                "/api/open/client/page", request("pageNo", 1), authHeaders(readonlyToken.getToken()), Map.class));
        assertTrue(((Number) page.get("count")).longValue() >= 1L);
    }

    private boolean containsById(List<Map<String, Object>> records, String id) {
        assertNotNull(records);
        for (Map<String, Object> record : records) {
            if (id.equals(String.valueOf(record.get("id")))) {
                return true;
            }
        }
        return false;
    }
}
