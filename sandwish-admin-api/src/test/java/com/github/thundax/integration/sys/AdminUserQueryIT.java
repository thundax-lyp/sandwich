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

public class AdminUserQueryIT extends AbstractAdminApiIT {

    private static final String ADMIN_USER_ID = "9100000000000000101";
    private static final String ENGINEERING_DEPARTMENT_ID = "9100000000000000002";
    private static final String ADMIN_ROLE_ID = "9100000000000000401";

    private AuthToken token;

    @Before
    public void prepareData() {
        prepareIntegrationData();
        token = loginAdmin();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReadUserDetailListPageAndReferenceData() {
        Map<String, Object> user = dataMap(httpClient.postJson(
                "/api/sys/user/get", request("id", ADMIN_USER_ID), authHeaders(token.getToken()), Map.class));
        assertEquals("it-admin", user.get("loginName"));
        assertEquals("Integration Admin", user.get("name"));
        assertEquals(Boolean.TRUE, user.get("superAdmin"));
        assertEquals(ENGINEERING_DEPARTMENT_ID, ((Map<String, Object>) user.get("department")).get("id"));
        assertTrue(containsById((List<Map<String, Object>>) user.get("roles"), ADMIN_ROLE_ID));

        Map<String, Object> query = request("loginName", "it-admin");
        query.put("departmentId", ENGINEERING_DEPARTMENT_ID);
        List<Map<String, Object>> users = (List<Map<String, Object>>)
                data(httpClient.postJson("/api/sys/user/list", query, authHeaders(token.getToken()), Map.class));
        assertTrue(containsById(users, ADMIN_USER_ID));

        Map<String, Object> pageQuery = request("pageNo", 1);
        pageQuery.put("pageSize", 10);
        pageQuery.put("enable", true);
        Map<String, Object> page =
                dataMap(httpClient.postJson("/api/sys/user/page", pageQuery, authHeaders(token.getToken()), Map.class));
        assertTrue(((Number) page.get("count")).longValue() >= 2L);
        assertTrue(containsById((List<Map<String, Object>>) page.get("records"), ADMIN_USER_ID));

        assertEquals(
                Boolean.TRUE,
                data(httpClient.postJson(
                        "/api/sys/user/check",
                        request("id", ADMIN_USER_ID, "loginName", "it-admin"),
                        authHeaders(token.getToken()),
                        Map.class)));
        assertEquals(
                Boolean.TRUE,
                data(httpClient.postJson(
                        "/api/sys/user/check",
                        request("loginName", "it-new-query-user"),
                        authHeaders(token.getToken()),
                        Map.class)));

        List<Map<String, Object>> departments = (List<Map<String, Object>>) data(httpClient.postJson(
                "/api/sys/user/department/tree", Collections.emptyMap(), authHeaders(token.getToken()), Map.class));
        assertTrue(containsById(departments, ENGINEERING_DEPARTMENT_ID));

        List<Map<String, Object>> roles = (List<Map<String, Object>>) data(httpClient.postJson(
                "/api/sys/user/role/list", Collections.emptyMap(), authHeaders(token.getToken()), Map.class));
        assertTrue(containsById(roles, ADMIN_ROLE_ID));
    }

    private Map<String, Object> request(String firstName, Object firstValue, String secondName, Object secondValue) {
        Map<String, Object> request = request(firstName, firstValue);
        request.put(secondName, secondValue);
        return request;
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
