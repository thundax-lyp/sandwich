package com.github.thundax.integration.submission;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.github.thundax.integration.AbstractOpenApiIT;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class OpenSubmissionQueryIT extends AbstractOpenApiIT {

    private static final String PAGE_PATH = "/api/submission/submission/page";
    private static final String APPROVED_SUBMISSION_ID = "9100000000000080002";

    @Before
    public void prepareData() {
        prepareIntegrationData();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldPageSubmissionsByStatus() {
        Map<String, Object> request = request("pageNo", 1);
        request.put("pageSize", 5);
        request.put("status", "APPROVED");
        request.put("sortDirection", "ASC");

        Map<String, Object> page = dataMap(postSignedJson(PAGE_PATH, request, Map.class));

        assertEquals(1, ((Number) page.get("count")).intValue());
        List<Map<String, Object>> records = (List<Map<String, Object>>) page.get("records");
        assertNotNull(records);
        assertEquals(APPROVED_SUBMISSION_ID, String.valueOf(records.get(0).get("id")));
        assertEquals("Integration Approved Content", records.get(0).get("title"));
    }
}
