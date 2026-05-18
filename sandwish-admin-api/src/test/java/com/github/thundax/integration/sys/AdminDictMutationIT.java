package com.github.thundax.integration.sys;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.github.thundax.common.test.integration.IntegrationAuthClient.AuthToken;
import com.github.thundax.integration.AbstractAdminApiIT;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

public class AdminDictMutationIT extends AbstractAdminApiIT {

    private AuthToken token;

    @Before
    public void prepareData() {
        prepareIntegrationData();
        token = loginAdmin();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldCreateUpdateSortAndDeleteDict() {
        String firstId = createDict("Integration Mutable Dict A", "A");
        String secondId = createDict("Integration Mutable Dict B", "B");

        Map<String, Object> updated = dataMap(httpClient.postJson(
                "/api/sys/dict/update",
                dictSaveRequest(firstId, "Integration Mutable Dict Updated", "UPDATED"),
                authHeaders(token.getToken()),
                Map.class));
        assertEquals("Integration Mutable Dict Updated", updated.get("label"));
        assertEquals("UPDATED", updated.get("value"));

        List<Map<String, Object>> mutableDicts = (List<Map<String, Object>>) data(httpClient.postJson(
                "/api/sys/dict/list", request("type", "it.mutable"), authHeaders(token.getToken()), Map.class));
        List<String> orderedIds = toIds(mutableDicts);
        Collections.reverse(orderedIds);
        assertEquals(
                Boolean.TRUE,
                data(httpClient.postJson(
                        "/api/sys/dict/sort", sortRequest(orderedIds), authHeaders(token.getToken()), Map.class)));

        assertEquals(
                Boolean.TRUE,
                data(httpClient.postJson(
                        "/api/sys/dict/delete",
                        Arrays.asList(request("id", firstId), request("id", secondId)),
                        authHeaders(token.getToken()),
                        Map.class)));
        assertNotFound("/api/sys/dict/update", dictSaveRequest(firstId, "Deleted Dict", "DELETED"));
    }

    @Test
    public void shouldRejectReadonlyUserWhenEditingDict() {
        AuthToken readonlyToken = loginAdmin("it-user", "Q1w2e3r$", "8888");
        try {
            httpClient.postJson(
                    "/api/sys/dict/create",
                    dictSaveRequest(null, "Forbidden Dict", "FORBIDDEN"),
                    authHeaders(readonlyToken.getToken()),
                    Map.class);
        } catch (HttpClientErrorException expected) {
            assertEquals(HttpStatus.FORBIDDEN, expected.getStatusCode());
            return;
        }
        fail("readonly user should not create dict");
    }

    private String createDict(String label, String value) {
        Map<String, Object> created = dataMap(httpClient.postJson(
                "/api/sys/dict/create", dictSaveRequest(null, label, value), authHeaders(token.getToken()), Map.class));
        String id = String.valueOf(created.get("id"));
        assertNotNull(id);
        return id;
    }

    private Map<String, Object> dictSaveRequest(String id, String label, String value) {
        Map<String, Object> request = request("type", "it.mutable");
        request.put("id", id);
        request.put("label", label);
        request.put("value", value);
        request.put("remarks", "integration dict mutation");
        return request;
    }

    private Map<String, Object> sortRequest(List<String> orderedIds) {
        Map<String, Object> request = request("orderedIds", orderedIds);
        request.put("sortDirection", "ASC");
        return request;
    }

    private List<String> toIds(List<Map<String, Object>> records) {
        assertTrue(records.size() >= 2);
        List<String> ids = new ArrayList<String>();
        for (Map<String, Object> record : records) {
            ids.add(String.valueOf(record.get("id")));
        }
        return ids;
    }

    private void assertNotFound(String path, Object request) {
        try {
            httpClient.postJson(path, request, authHeaders(token.getToken()), Map.class);
        } catch (HttpClientErrorException expected) {
            assertEquals(HttpStatus.NOT_FOUND, expected.getStatusCode());
            return;
        }
        fail("resource should not exist: " + path);
    }
}
