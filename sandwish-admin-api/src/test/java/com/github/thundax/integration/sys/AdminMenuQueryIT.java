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

public class AdminMenuQueryIT extends AbstractAdminApiIT {

    private static final String ROOT_MENU_ID = "9100000000000001001";
    private static final String USER_MENU_ID = "9100000000000001002";
    private static final String USER_VIEW_MENU_ID = "9100000000000001003";

    private AuthToken token;

    @Before
    public void prepareData() {
        prepareIntegrationData();
        token = loginAdmin();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReadMenuGetListAndTree() {
        Map<String, Object> menu = dataMap(httpClient.postJson(
                "/api/sys/menu/get", request("id", USER_VIEW_MENU_ID), authHeaders(token.getToken()), Map.class));
        assertEquals("Integration User View", menu.get("name"));
        assertEquals("sys:user:view", menu.get("perms"));

        List<Map<String, Object>> children = (List<Map<String, Object>>) data(httpClient.postJson(
                "/api/sys/menu/list", request("parentId", USER_MENU_ID), authHeaders(token.getToken()), Map.class));
        assertTrue(containsById(children, USER_VIEW_MENU_ID));

        List<Map<String, Object>> tree = (List<Map<String, Object>>) data(httpClient.postJson(
                "/api/sys/menu/tree",
                Collections.singletonList(request("id", USER_MENU_ID)),
                authHeaders(token.getToken()),
                Map.class));
        assertTrue(containsById(tree, ROOT_MENU_ID));
        assertFalseContainsById(tree, USER_MENU_ID);
        assertFalseContainsById(tree, USER_VIEW_MENU_ID);
    }

    @Test
    public void shouldRejectReadonlyUserWhenReadingSuperMenuEndpoint() {
        AuthToken readonlyToken = loginAdmin("it-user", "Q1w2e3r$", "8888");
        try {
            httpClient.postJson(
                    "/api/sys/menu/list", Collections.emptyMap(), authHeaders(readonlyToken.getToken()), Map.class);
        } catch (HttpClientErrorException expected) {
            assertEquals(HttpStatus.FORBIDDEN, expected.getStatusCode());
            return;
        }
        fail("readonly user should not read super menu endpoint");
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
