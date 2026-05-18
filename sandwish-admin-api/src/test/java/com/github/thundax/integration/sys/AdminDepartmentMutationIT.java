package com.github.thundax.integration.sys;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.github.thundax.common.test.integration.IntegrationAuthClient.AuthToken;
import com.github.thundax.integration.AbstractAdminApiIT;
import java.util.Arrays;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

public class AdminDepartmentMutationIT extends AbstractAdminApiIT {

    private static final String ROOT_DEPARTMENT_ID = "9100000000000000001";

    private AuthToken token;

    @Before
    public void prepareData() {
        prepareIntegrationData();
        token = loginAdmin();
    }

    @Test
    public void shouldCreateUpdateMoveAndDeleteDepartment() {
        String firstId = createDepartment("Integration Mutable Department A", "IT Mut A");
        String secondId = createDepartment("Integration Mutable Department B", "IT Mut B");

        Map<String, Object> updated = dataMap(httpClient.postJson(
                "/api/sys/department/update",
                departmentSaveRequest(
                        firstId, ROOT_DEPARTMENT_ID, "Integration Mutable Department Updated", "IT Mut U"),
                authHeaders(token.getToken()),
                Map.class));
        assertEquals("Integration Mutable Department Updated", updated.get("name"));

        assertEquals(
                Boolean.TRUE,
                data(httpClient.postJson(
                        "/api/sys/department/move",
                        moveRequest(secondId, firstId),
                        authHeaders(token.getToken()),
                        Map.class)));

        assertEquals(
                Boolean.TRUE,
                data(httpClient.postJson(
                        "/api/sys/department/delete",
                        Arrays.asList(request("id", firstId), request("id", secondId)),
                        authHeaders(token.getToken()),
                        Map.class)));
        assertNotFound("/api/sys/department/get", request("id", firstId));
    }

    @Test
    public void shouldRejectReadonlyUserWhenEditingDepartment() {
        AuthToken readonlyToken = loginAdmin("it-user", "Q1w2e3r$", "8888");
        try {
            httpClient.postJson(
                    "/api/sys/department/create",
                    departmentSaveRequest(null, ROOT_DEPARTMENT_ID, "Forbidden Department", "Forbidden"),
                    authHeaders(readonlyToken.getToken()),
                    Map.class);
        } catch (HttpClientErrorException expected) {
            assertEquals(HttpStatus.FORBIDDEN, expected.getStatusCode());
            return;
        }
        fail("readonly user should not create department");
    }

    private String createDepartment(String name, String shortName) {
        Map<String, Object> created = dataMap(httpClient.postJson(
                "/api/sys/department/create",
                departmentSaveRequest(null, ROOT_DEPARTMENT_ID, name, shortName),
                authHeaders(token.getToken()),
                Map.class));
        String id = String.valueOf(created.get("id"));
        assertNotNull(id);
        return id;
    }

    private Map<String, Object> departmentSaveRequest(String id, String parentId, String name, String shortName) {
        Map<String, Object> request = request("name", name);
        request.put("id", id);
        request.put("parentId", parentId);
        request.put("shortName", shortName);
        request.put("remarks", "integration department mutation");
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
