package com.github.thundax.integration.sys;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.github.thundax.common.test.integration.IntegrationAuthClient.AuthToken;
import com.github.thundax.integration.AbstractAdminApiIT;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

public class AdminMenuMutationIT extends AbstractAdminApiIT {

    private static final String ROOT_MENU_ID = "9100000000000001001";

    private AuthToken token;

    @Before
    public void prepareData() {
        prepareIntegrationData();
        token = loginAdmin();
    }

    @Test
    public void shouldCreateUpdateDisplayMoveAndDeleteMenu() {
        String firstId = createMenu("Integration Mutable Menu A", "it:mutable:a");
        String secondId = createMenu("Integration Mutable Menu B", "it:mutable:b");

        Map<String, Object> updated = dataMap(httpClient.postJson(
                "/api/sys/menu/update",
                menuSaveRequest(firstId, ROOT_MENU_ID, "Integration Mutable Menu Updated", "it:mutable:update"),
                authHeaders(token.getToken()),
                Map.class));
        assertEquals("Integration Mutable Menu Updated", updated.get("name"));

        assertEquals(
                Boolean.TRUE,
                data(httpClient.postJson(
                        "/api/sys/menu/display",
                        Collections.singletonList(displayRequest(firstId, false)),
                        authHeaders(token.getToken()),
                        Map.class)));
        Map<String, Object> hidden = dataMap(httpClient.postJson(
                "/api/sys/menu/get", request("id", firstId), authHeaders(token.getToken()), Map.class));
        assertEquals(Boolean.FALSE, hidden.get("display"));

        assertEquals(
                Boolean.TRUE,
                data(httpClient.postJson(
                        "/api/sys/menu/move",
                        moveRequest(secondId, firstId),
                        authHeaders(token.getToken()),
                        Map.class)));

        assertEquals(
                Boolean.TRUE,
                data(httpClient.postJson(
                        "/api/sys/menu/delete",
                        Arrays.asList(request("id", firstId), request("id", secondId)),
                        authHeaders(token.getToken()),
                        Map.class)));
        assertNotFound("/api/sys/menu/get", request("id", firstId));
    }

    @Test
    public void shouldRejectReadonlyUserWhenEditingMenu() {
        AuthToken readonlyToken = loginAdmin("it-user", "Q1w2e3r$", "8888");
        try {
            httpClient.postJson(
                    "/api/sys/menu/create",
                    menuSaveRequest(null, ROOT_MENU_ID, "Forbidden Menu", "it:forbidden"),
                    authHeaders(readonlyToken.getToken()),
                    Map.class);
        } catch (HttpClientErrorException expected) {
            assertEquals(HttpStatus.FORBIDDEN, expected.getStatusCode());
            return;
        }
        fail("readonly user should not create menu");
    }

    private String createMenu(String name, String perms) {
        Map<String, Object> created = dataMap(httpClient.postJson(
                "/api/sys/menu/create",
                menuSaveRequest(null, ROOT_MENU_ID, name, perms),
                authHeaders(token.getToken()),
                Map.class));
        String id = String.valueOf(created.get("id"));
        assertNotNull(id);
        return id;
    }

    private Map<String, Object> menuSaveRequest(String id, String parentId, String name, String perms) {
        Map<String, Object> request = request("name", name);
        request.put("id", id);
        request.put("parentId", parentId);
        request.put("remarks", "integration menu mutation");
        request.put("perms", perms);
        request.put("ranks", 1);
        request.put("display", true);
        request.put("displayParams", "{}");
        request.put("url", "/integration/menu");
        return request;
    }

    private Map<String, Object> displayRequest(String id, boolean display) {
        Map<String, Object> request = request("id", id);
        request.put("display", display);
        return request;
    }

    private Map<String, Object> moveRequest(String fromId, String toId) {
        Map<String, Object> request = request("fromNodeId", fromId);
        request.put("toNodeId", toId);
        request.put("type", "after");
        return request;
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
