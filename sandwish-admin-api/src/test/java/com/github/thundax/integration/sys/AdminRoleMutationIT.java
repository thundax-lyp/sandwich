package com.github.thundax.integration.sys;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.github.thundax.common.test.integration.IntegrationAuthClient.AuthToken;
import com.github.thundax.integration.AbstractAdminApiIT;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

public class AdminRoleMutationIT extends AbstractAdminApiIT {

    private static final String ROLE_ID = "9100000000000000410";
    private static final String SECOND_ROLE_ID = "9100000000000000411";
    private static final String ADMIN_USER_ID = "9100000000000000101";
    private static final String READONLY_USER_ID = "9100000000000000102";
    private static final String USER_VIEW_MENU_ID = "9100000000000001003";
    private static final String USER_EDIT_MENU_ID = "9100000000000001004";

    private AuthToken token;

    @Before
    public void prepareData() {
        prepareIntegrationData();
        token = loginAdmin();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldCreateUpdateStatusSortAssignAndDeleteRole() {
        Map<String, Object> created = dataMap(httpClient.postJson(
                "/api/sys/role/create",
                roleSaveRequest(ROLE_ID, "Integration Mutable Role", USER_VIEW_MENU_ID),
                authHeaders(token.getToken()),
                Map.class));
        String roleId = String.valueOf(created.get("id"));
        assertNotNull(roleId);
        assertTrue(containsById((List<Map<String, Object>>) created.get("menus"), USER_VIEW_MENU_ID));

        Map<String, Object> updated = dataMap(httpClient.postJson(
                "/api/sys/role/update",
                roleSaveRequest(roleId, "Integration Mutable Role Updated", USER_VIEW_MENU_ID, USER_EDIT_MENU_ID),
                authHeaders(token.getToken()),
                Map.class));
        assertEquals("Integration Mutable Role Updated", updated.get("name"));
        assertTrue(containsById((List<Map<String, Object>>) updated.get("menus"), USER_EDIT_MENU_ID));

        assertEquals(
                Boolean.TRUE,
                data(httpClient.postJson(
                        "/api/sys/role/enable",
                        Collections.singletonList(statusRequest(roleId, false)),
                        authHeaders(token.getToken()),
                        Map.class)));
        Map<String, Object> disabled = dataMap(httpClient.postJson(
                "/api/sys/role/get", request("id", roleId), authHeaders(token.getToken()), Map.class));
        assertEquals(Boolean.FALSE, disabled.get("enable"));

        assertEquals(
                Boolean.TRUE,
                data(httpClient.postJson(
                        "/api/sys/role/enable",
                        Collections.singletonList(statusRequest(roleId, true)),
                        authHeaders(token.getToken()),
                        Map.class)));

        Map<String, Object> secondRole = dataMap(httpClient.postJson(
                "/api/sys/role/create",
                roleSaveRequest(SECOND_ROLE_ID, "Integration Second Role", USER_VIEW_MENU_ID),
                authHeaders(token.getToken()),
                Map.class));
        String secondRoleId = String.valueOf(secondRole.get("id"));
        List<Map<String, Object>> roles = (List<Map<String, Object>>) data(httpClient.postJson(
                "/api/sys/role/list", Collections.emptyMap(), authHeaders(token.getToken()), Map.class));
        assertEquals(
                Boolean.TRUE,
                data(httpClient.postJson(
                        "/api/sys/role/sort",
                        sortRequest(reversedRoleIds(roles)),
                        authHeaders(token.getToken()),
                        Map.class)));

        assertEquals(
                Boolean.TRUE,
                data(httpClient.postJson(
                        "/api/sys/role/user/assign",
                        assignRequest(roleId, ADMIN_USER_ID, READONLY_USER_ID),
                        authHeaders(token.getToken()),
                        Map.class)));
        List<Map<String, Object>> users = (List<Map<String, Object>>) data(httpClient.postJson(
                "/api/sys/role/user/list", request("id", roleId), authHeaders(token.getToken()), Map.class));
        assertTrue(containsById(users, ADMIN_USER_ID));
        assertTrue(containsById(users, READONLY_USER_ID));

        assertEquals(
                Boolean.TRUE,
                data(httpClient.postJson(
                        "/api/sys/role/delete",
                        Arrays.asList(request("id", roleId), request("id", secondRoleId)),
                        authHeaders(token.getToken()),
                        Map.class)));
        assertNotFound("/api/sys/role/get", request("id", roleId));
    }

    @Test
    public void shouldRejectReadonlyUserWhenEditingRole() {
        AuthToken readonlyToken = loginAdmin("it-user", "Q1w2e3r$", "8888");
        try {
            httpClient.postJson(
                    "/api/sys/role/create",
                    roleSaveRequest(ROLE_ID, "Forbidden Role", USER_VIEW_MENU_ID),
                    authHeaders(readonlyToken.getToken()),
                    Map.class);
        } catch (HttpClientErrorException expected) {
            assertEquals(HttpStatus.FORBIDDEN, expected.getStatusCode());
            return;
        }
        fail("readonly user should not create role");
    }

    private Map<String, Object> roleSaveRequest(String id, String name, String... menuIds) {
        Map<String, Object> request = request("id", id);
        request.put("remarks", "integration role mutation");
        request.put("name", name);
        request.put("admin", false);
        request.put("enable", true);
        request.put("menus", menuList(menuIds));
        return request;
    }

    private List<Map<String, Object>> menuList(String... menuIds) {
        java.util.ArrayList<Map<String, Object>> menus = new java.util.ArrayList<Map<String, Object>>();
        for (String menuId : menuIds) {
            menus.add(request("id", menuId));
        }
        return menus;
    }

    private Map<String, Object> statusRequest(String id, boolean enable) {
        Map<String, Object> request = request("id", id);
        request.put("enable", enable);
        return request;
    }

    private Map<String, Object> sortRequest(List<String> roleIds) {
        Map<String, Object> request = request("orderedIds", roleIds);
        request.put("sortDirection", "ASC");
        return request;
    }

    private List<String> reversedRoleIds(List<Map<String, Object>> roles) {
        java.util.ArrayList<String> ids = new java.util.ArrayList<String>();
        for (Map<String, Object> role : roles) {
            ids.add(String.valueOf(role.get("id")));
        }
        Collections.reverse(ids);
        return ids;
    }

    private Map<String, Object> assignRequest(String roleId, String... userIds) {
        Map<String, Object> request = request("roleId", roleId);
        java.util.ArrayList<Map<String, Object>> users = new java.util.ArrayList<Map<String, Object>>();
        for (String userId : userIds) {
            users.add(request("id", userId));
        }
        request.put("users", users);
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
