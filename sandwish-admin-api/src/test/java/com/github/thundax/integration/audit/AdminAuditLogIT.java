package com.github.thundax.integration.audit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.github.thundax.common.test.integration.IntegrationAuthClient.AuthToken;
import com.github.thundax.integration.AbstractAdminApiIT;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class AdminAuditLogIT extends AbstractAdminApiIT {

    private static final String DEPARTMENT_ID = "9100000000000000002";
    private static final String UPDATE_LOG_ID = "9100000000000090102";

    private AuthToken token;

    @Before
    public void prepareData() {
        prepareIntegrationData();
        token = loginAdmin();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReadAuditMetaHistoryDetailAndObjectPages() {
        Map<String, Object> meta = dataMap(
                httpClient.postJson("/api/audit/log/meta", objectRequest(), authHeaders(token.getToken()), Map.class));
        assertEquals("Department", meta.get("objectType"));
        assertEquals(DEPARTMENT_ID, meta.get("objectId"));
        assertEquals(2, ((Number) meta.get("version")).intValue());
        assertEquals("UPDATE", meta.get("lastAction"));

        List<Map<String, Object>> history = (List<Map<String, Object>>) data(httpClient.postJson(
                "/api/audit/log/history", objectRequest(), authHeaders(token.getToken()), Map.class));
        assertEquals(2, history.size());
        assertContainsAction(history, "CREATE");
        assertContainsAction(history, "UPDATE");

        Map<String, Object> detail = dataMap(httpClient.postJson(
                "/api/audit/log/detail", request("id", UPDATE_LOG_ID), authHeaders(token.getToken()), Map.class));
        assertEquals("UPDATE", detail.get("action"));
        assertEquals("Integration Admin", detail.get("operatorName"));
        assertEquals(1, ((Number) detail.get("changedFieldCount")).intValue());
        assertEquals("Integration Engineering Team", detail.get("objectDisplayName"));
        assertNotNull(detail.get("beforeSnapshot"));
        assertNotNull(detail.get("afterSnapshot"));

        Map<String, Object> overview = dataMap(httpClient.postJson(
                "/api/audit/log/object/overview", objectRequest(), authHeaders(token.getToken()), Map.class));
        assertEquals(2, ((Number) ((Map<String, Object>) overview.get("meta")).get("version")).intValue());
        assertTrue(((List<Map<String, Object>>) overview.get("latestLogs")).size() >= 1);

        Map<String, Object> objectPageRequest = objectRequest();
        objectPageRequest.put("pageNo", 1);
        objectPageRequest.put("pageSize", 5);
        Map<String, Object> objectPage = dataMap(httpClient.postJson(
                "/api/audit/log/object/page", objectPageRequest, authHeaders(token.getToken()), Map.class));
        assertEquals(2, ((Number) objectPage.get("count")).intValue());

        Map<String, Object> page = dataMap(
                httpClient.postJson("/api/audit/log/page", logPageRequest(), authHeaders(token.getToken()), Map.class));
        assertEquals(1, ((Number) page.get("count")).intValue());
        List<Map<String, Object>> records = (List<Map<String, Object>>) page.get("records");
        assertEquals(UPDATE_LOG_ID, String.valueOf(records.get(0).get("id")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReadAuditOptionsFieldsAndReadonlyAuditView() {
        Map<String, Object> options = dataMap(httpClient.postJson(
                "/api/audit/log/options",
                new java.util.LinkedHashMap<String, Object>(),
                authHeaders(token.getToken()),
                Map.class));
        assertContainsOption((List<Map<String, Object>>) options.get("objectTypes"), "Department");
        assertContainsOption((List<Map<String, Object>>) options.get("actions"), "UPDATE");
        assertContainsOption((List<Map<String, Object>>) options.get("operatorTypes"), "USER");

        List<Map<String, Object>> fields = (List<Map<String, Object>>) data(httpClient.postJson(
                "/api/audit/log/fields",
                request("objectType", "Submission"),
                authHeaders(token.getToken()),
                Map.class));
        assertContainsField(fields, "title");
        assertContainsField(fields, "content");
        assertContainsField(fields, "status");

        AuthToken readonlyToken = loginAdmin("it-user", "Q1w2e3r$", "8888");
        Map<String, Object> readonlyPage = dataMap(httpClient.postJson(
                "/api/audit/log/page", logPageRequest(), authHeaders(readonlyToken.getToken()), Map.class));
        assertEquals(1, ((Number) readonlyPage.get("count")).intValue());
    }

    private Map<String, Object> objectRequest() {
        Map<String, Object> request = request("objectType", "Department");
        request.put("objectId", DEPARTMENT_ID);
        return request;
    }

    private Map<String, Object> logPageRequest() {
        Map<String, Object> request = request("pageNo", 1);
        request.put("pageSize", 5);
        request.put("objectType", "Department");
        request.put("objectId", DEPARTMENT_ID);
        request.put("action", "UPDATE");
        request.put("operatorType", "USER");
        request.put("requestId", "it-request-update");
        return request;
    }

    private void assertContainsOption(List<Map<String, Object>> options, String value) {
        assertNotNull(options);
        for (Map<String, Object> option : options) {
            if (value.equals(option.get("value"))) {
                return;
            }
        }
        assertTrue("missing option " + value, false);
    }

    private void assertContainsAction(List<Map<String, Object>> logs, String action) {
        assertNotNull(logs);
        for (Map<String, Object> log : logs) {
            if (action.equals(log.get("action"))) {
                return;
            }
        }
        assertTrue("missing action " + action, false);
    }

    private void assertContainsField(List<Map<String, Object>> fields, String fieldName) {
        assertNotNull(fields);
        for (Map<String, Object> field : fields) {
            if (fieldName.equals(field.get("fieldName"))) {
                return;
            }
        }
        assertTrue("missing field " + fieldName, false);
    }
}
