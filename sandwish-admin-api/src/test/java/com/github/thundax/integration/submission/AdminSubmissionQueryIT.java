package com.github.thundax.integration.submission;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.github.thundax.common.test.integration.IntegrationAuthClient.AuthToken;
import com.github.thundax.integration.AbstractAdminApiIT;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class AdminSubmissionQueryIT extends AbstractAdminApiIT {

    private static final String SUBMITTED_SUBMISSION_ID = "9100000000000080001";
    private static final String APPROVED_SUBMISSION_ID = "9100000000000080002";
    private static final String SUBMISSION_IMAGE_ID = "9100000000000070001";

    private AuthToken token;

    @Before
    public void prepareData() {
        prepareIntegrationData();
        token = loginAdmin();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldGetAndPageSubmissions() {
        Map<String, Object> submission = dataMap(httpClient.postJson(
                "/api/submission/submission/get",
                request("id", SUBMITTED_SUBMISSION_ID),
                authHeaders(token.getToken()),
                Map.class));
        assertEquals("Integration Submitted Content", submission.get("title"));
        assertEquals("SUBMITTED", submission.get("status"));
        assertTrue(((List<String>) submission.get("imageObjectIds")).contains(SUBMISSION_IMAGE_ID));

        Map<String, Object> pageRequest = request("pageNo", 1);
        pageRequest.put("pageSize", 5);
        pageRequest.put("status", "APPROVED");
        pageRequest.put("sortDirection", "ASC");

        Map<String, Object> page = dataMap(httpClient.postJson(
                "/api/submission/submission/page", pageRequest, authHeaders(token.getToken()), Map.class));
        assertEquals(1, ((Number) page.get("count")).intValue());
        List<Map<String, Object>> records = (List<Map<String, Object>>) page.get("records");
        assertNotNull(records);
        assertEquals(APPROVED_SUBMISSION_ID, String.valueOf(records.get(0).get("id")));
        assertEquals("Integration Approved Content", records.get(0).get("title"));
    }

    @Test
    public void shouldAllowReadonlyUserWhenViewingSubmissions() {
        AuthToken readonlyToken = loginAdmin("it-user", "Q1w2e3r$", "8888");
        Map<String, Object> pageRequest = request("pageNo", 1);
        pageRequest.put("pageSize", 5);
        Map<String, Object> page = dataMap(httpClient.postJson(
                "/api/submission/submission/page", pageRequest, authHeaders(readonlyToken.getToken()), Map.class));
        assertTrue(((Number) page.get("count")).intValue() >= 1);
    }
}
