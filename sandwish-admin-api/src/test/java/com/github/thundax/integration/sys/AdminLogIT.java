package com.github.thundax.integration.sys;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.github.thundax.common.test.integration.IntegrationAuthClient.AuthToken;
import com.github.thundax.integration.AbstractAdminApiIT;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

public class AdminLogIT extends AbstractAdminApiIT {

    private static final String ADMIN_LOG_ID = "9100000000000090201";
    private static final String USER_LOG_ID = "9100000000000090202";

    private AuthToken token;

    @Before
    public void prepareData() {
        prepareIntegrationData();
        token = loginAdmin();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldPageSystemLogsByTitleAndUserLoginName() {
        Map<String, Object> adminPage = dataMap(httpClient.postJson(
                "/api/sys/log/page",
                pageRequest("title", "Integration admin system log"),
                authHeaders(token.getToken()),
                Map.class));
        assertTrue(((Number) adminPage.get("count")).longValue() >= 1L);
        List<Map<String, Object>> adminRecords = (List<Map<String, Object>>) adminPage.get("records");
        assertTrue(containsById(adminRecords, ADMIN_LOG_ID));
        Map<String, Object> adminLog = findById(adminRecords, ADMIN_LOG_ID);
        assertEquals("/api/sys/log/page", adminLog.get("requestUri"));
        assertEquals("it-admin", ((Map<String, Object>) adminLog.get("createUser")).get("loginName"));

        Map<String, Object> userRequest = pageRequest("userLoginName", "it-user");
        userRequest.put("title", "Integration user system log");
        Map<String, Object> userPage = dataMap(
                httpClient.postJson("/api/sys/log/page", userRequest, authHeaders(token.getToken()), Map.class));
        assertTrue(containsById((List<Map<String, Object>>) userPage.get("records"), USER_LOG_ID));
    }

    @Test
    public void shouldRejectReadonlyUserWhenReadingSuperLogEndpoint() {
        AuthToken readonlyToken = loginAdmin("it-user", "Q1w2e3r$", "8888");
        try {
            httpClient.postJson(
                    "/api/sys/log/page",
                    pageRequest("title", "Integration"),
                    authHeaders(readonlyToken.getToken()),
                    Map.class);
        } catch (HttpClientErrorException expected) {
            assertEquals(HttpStatus.FORBIDDEN, expected.getStatusCode());
            return;
        }
        fail("readonly user should not read super log endpoint");
    }

    private Map<String, Object> pageRequest(String field, String value) {
        Map<String, Object> request = request("pageNo", 1);
        request.put("pageSize", 10);
        request.put(field, value);
        return request;
    }

    private boolean containsById(List<Map<String, Object>> records, String id) {
        return findById(records, id) != null;
    }

    private Map<String, Object> findById(List<Map<String, Object>> records, String id) {
        assertNotNull(records);
        for (Map<String, Object> record : records) {
            if (id.equals(String.valueOf(record.get("id")))) {
                return record;
            }
        }
        return null;
    }
}
