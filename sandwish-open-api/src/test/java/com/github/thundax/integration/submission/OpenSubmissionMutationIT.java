package com.github.thundax.integration.submission;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.github.thundax.integration.AbstractOpenApiIT;
import java.util.Collections;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class OpenSubmissionMutationIT extends AbstractOpenApiIT {

    private static final String CREATE_PATH = "/api/submission/submission/create";
    private static final String CHANGE_STATUS_PATH = "/api/submission/submission/change-status";

    @Before
    public void prepareData() {
        prepareIntegrationData();
    }

    @Test
    public void shouldCreateAndChangeSubmissionStatus() {
        Map<String, Object> created = dataMap(postSignedJson(
                CREATE_PATH,
                submissionRequest("Integration OpenAPI Mutable Submission", "Integration OpenAPI content."),
                Map.class));
        String submissionId = String.valueOf(created.get("id"));
        assertNotNull(submissionId);
        assertEquals("SUBMITTED", created.get("status"));

        assertEquals(
                Boolean.TRUE,
                data(postSignedJson(CHANGE_STATUS_PATH, statusRequest(submissionId, "APPROVED"), Map.class)));
    }

    private Map<String, Object> submissionRequest(String title, String content) {
        Map<String, Object> request = request("title", title);
        request.put("content", content);
        request.put("imageObjectIds", Collections.emptyList());
        return request;
    }

    private Map<String, Object> statusRequest(String id, String status) {
        Map<String, Object> request = request("id", id);
        request.put("status", status);
        return request;
    }
}
