package com.github.thundax.integration.sys;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.github.thundax.common.test.integration.IntegrationAuthClient.AuthToken;
import com.github.thundax.integration.AbstractAdminApiIT;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class AdminRoleQueryIT extends AbstractAdminApiIT {

    private static final String ADMIN_ROLE_ID = "9100000000000000401";
    private static final String ADMIN_USER_ID = "9100000000000000101";
    private static final String USER_VIEW_MENU_ID = "9100000000000001003";
    private static final String ENGINEERING_DEPARTMENT_NODE_ID = "DEPARTMENT_9100000000000000002";

    private AuthToken token;

    @Before
    public void prepareData() {
        prepareIntegrationData();
        token = loginAdmin();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReadRoleDetailListMenuTreeAndUsers() {
        Map<String, Object> role = dataMap(httpClient.postJson(
                "/api/sys/role/get", request("id", ADMIN_ROLE_ID), authHeaders(token.getToken()), Map.class));
        assertEquals("Integration Administrator", role.get("name"));
        assertEquals(Boolean.TRUE, role.get("admin"));
        assertTrue(containsById((List<Map<String, Object>>) role.get("menus"), USER_VIEW_MENU_ID));

        List<Map<String, Object>> roles = (List<Map<String, Object>>) data(httpClient.postJson(
                "/api/sys/role/list", request("enable", true), authHeaders(token.getToken()), Map.class));
        assertTrue(containsById(roles, ADMIN_ROLE_ID));

        List<Map<String, Object>> menus = (List<Map<String, Object>>) data(httpClient.postJson(
                "/api/sys/role/menu/tree", Collections.emptyMap(), authHeaders(token.getToken()), Map.class));
        assertTrue(containsById(menus, USER_VIEW_MENU_ID));

        List<Map<String, Object>> userTree = (List<Map<String, Object>>) data(httpClient.postJson(
                "/api/sys/role/user/tree", Collections.emptyMap(), authHeaders(token.getToken()), Map.class));
        assertTrue(containsById(userTree, ENGINEERING_DEPARTMENT_NODE_ID));
        assertTrue(containsById(userTree, ADMIN_USER_ID));

        List<Map<String, Object>> users = (List<Map<String, Object>>) data(httpClient.postJson(
                "/api/sys/role/user/list", request("id", ADMIN_ROLE_ID), authHeaders(token.getToken()), Map.class));
        assertTrue(containsById(users, ADMIN_USER_ID));
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
