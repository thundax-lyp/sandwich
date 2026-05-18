package com.github.thundax.integration.sys;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.github.thundax.common.test.integration.IntegrationAuthClient.AuthToken;
import com.github.thundax.integration.AbstractAdminApiIT;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

public class AdminDepartmentQueryIT extends AbstractAdminApiIT {

    private static final String ROOT_DEPARTMENT_ID = "9100000000000000001";
    private static final String ENGINEERING_DEPARTMENT_ID = "9100000000000000002";
    private static final String QA_DEPARTMENT_ID = "9100000000000000003";

    private AuthToken token;

    @Before
    public void prepareData() {
        prepareIntegrationData();
        token = loginAdmin();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReadDepartmentGetListAndTree() {
        Map<String, Object> department = dataMap(httpClient.postJson(
                "/api/sys/department/get",
                request("id", ENGINEERING_DEPARTMENT_ID),
                authHeaders(token.getToken()),
                Map.class));
        assertEquals("Integration Engineering", department.get("name"));
        assertTrue(String.valueOf(department.get("namePath")).contains("Integration Company"));

        List<Map<String, Object>> departments = (List<Map<String, Object>>) data(httpClient.postJson(
                "/api/sys/department/list",
                request("parentId", ENGINEERING_DEPARTMENT_ID),
                authHeaders(token.getToken()),
                Map.class));
        assertTrue(containsById(departments, QA_DEPARTMENT_ID));

        List<Map<String, Object>> tree = (List<Map<String, Object>>) data(httpClient.postJson(
                "/api/sys/department/tree",
                Collections.singletonList(request("id", ENGINEERING_DEPARTMENT_ID)),
                authHeaders(token.getToken()),
                Map.class));
        assertTrue(containsById(tree, ROOT_DEPARTMENT_ID));
        assertFalseContainsById(tree, ENGINEERING_DEPARTMENT_ID);
        assertFalseContainsById(tree, QA_DEPARTMENT_ID);
    }

    @Test
    public void shouldRejectReadonlyUserWhenReadingSuperDepartmentTree() {
        AuthToken readonlyToken = loginAdmin("it-user", "Q1w2e3r$", "8888");
        try {
            httpClient.postJson(
                    "/api/sys/department/tree",
                    Collections.emptyList(),
                    authHeaders(readonlyToken.getToken()),
                    Map.class);
        } catch (HttpClientErrorException expected) {
            assertEquals(HttpStatus.FORBIDDEN, expected.getStatusCode());
            return;
        }
        fail("readonly user should not read super department tree");
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

    private void assertFalseContainsById(List<Map<String, Object>> records, String id) {
        if (containsById(records, id)) {
            fail("record should be excluded: " + id);
        }
    }
}
