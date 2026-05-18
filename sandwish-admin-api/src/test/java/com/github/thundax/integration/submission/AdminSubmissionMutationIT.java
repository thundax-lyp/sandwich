package com.github.thundax.integration.submission;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.github.thundax.common.test.integration.IntegrationAuthClient.AuthToken;
import com.github.thundax.integration.AbstractAdminApiIT;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;

public class AdminSubmissionMutationIT extends AbstractAdminApiIT {

    private AuthToken token;

    @Before
    public void prepareData() {
        prepareIntegrationData();
        token = loginAdmin();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldUploadImageCreateChangeStatusSortAndDeleteSubmission() {
        byte[] image = new byte[] {(byte) 0xff, (byte) 0xd8, 1, 2, 3, (byte) 0xff, (byte) 0xd9};
        Map<String, Object> uploaded = dataMap(httpClient.postMultipart(
                "/api/submission/submission/image/upload",
                httpClient.multipartBody(null, "file", "integration-submission.jpg", image, MediaType.IMAGE_JPEG),
                authHeaders(token.getToken()),
                Map.class));
        String imageObjectId = String.valueOf(uploaded.get("id"));
        assertNotNull(imageObjectId);
        assertEquals("integration-submission.jpg", uploaded.get("originalFilename"));

        Map<String, Object> created = dataMap(httpClient.postJson(
                "/api/submission/submission/create",
                submissionRequest(
                        "Integration Mutable Submission", "Integration mutable submission content", imageObjectId),
                authHeaders(token.getToken()),
                Map.class));
        String submissionId = String.valueOf(created.get("id"));
        assertNotNull(submissionId);
        assertEquals("SUBMITTED", created.get("status"));
        assertEquals(Collections.singletonList(imageObjectId), created.get("imageObjectIds"));

        assertEquals(
                Boolean.TRUE,
                data(httpClient.postJson(
                        "/api/submission/submission/change-status",
                        statusRequest(submissionId, "APPROVED"),
                        authHeaders(token.getToken()),
                        Map.class)));
        Map<String, Object> approved = dataMap(httpClient.postJson(
                "/api/submission/submission/get",
                request("id", submissionId),
                authHeaders(token.getToken()),
                Map.class));
        assertEquals("APPROVED", approved.get("status"));

        List<Map<String, Object>> records = (List<Map<String, Object>>) dataMap(httpClient.postJson(
                        "/api/submission/submission/page", pageRequest(), authHeaders(token.getToken()), Map.class))
                .get("records");
        List<String> ids = ids(records);
        Collections.reverse(ids);
        assertEquals(
                Boolean.TRUE,
                data(httpClient.postJson(
                        "/api/submission/submission/sort",
                        sortRequest(ids),
                        authHeaders(token.getToken()),
                        Map.class)));

        assertEquals(
                Boolean.TRUE,
                data(httpClient.postJson(
                        "/api/submission/submission/delete",
                        Collections.singletonList(request("id", submissionId)),
                        authHeaders(token.getToken()),
                        Map.class)));
    }

    @Test
    public void shouldRejectReadonlyUserWhenCreatingSubmission() {
        AuthToken readonlyToken = loginAdmin("it-user", "Q1w2e3r$", "8888");
        try {
            httpClient.postJson(
                    "/api/submission/submission/create",
                    submissionRequest("Integration Forbidden Submission", "Integration forbidden content", null),
                    authHeaders(readonlyToken.getToken()),
                    Map.class);
        } catch (HttpClientErrorException expected) {
            assertEquals(HttpStatus.FORBIDDEN, expected.getStatusCode());
            return;
        }
        fail("readonly user should not create submission");
    }

    private Map<String, Object> submissionRequest(String title, String content, String imageObjectId) {
        Map<String, Object> request = request("title", title);
        request.put("content", content);
        request.put(
                "imageObjectIds",
                imageObjectId == null ? Collections.emptyList() : Collections.singletonList(imageObjectId));
        return request;
    }

    private Map<String, Object> statusRequest(String id, String status) {
        Map<String, Object> request = request("id", id);
        request.put("status", status);
        return request;
    }

    private Map<String, Object> pageRequest() {
        Map<String, Object> request = request("pageNo", 1);
        request.put("pageSize", 20);
        request.put("sortDirection", "ASC");
        return request;
    }

    private Map<String, Object> sortRequest(List<String> orderedIds) {
        Map<String, Object> request = request("orderedIds", orderedIds);
        request.put("sortDirection", "ASC");
        return request;
    }

    private List<String> ids(List<Map<String, Object>> records) {
        assertNotNull(records);
        List<String> result = new ArrayList<String>();
        for (Map<String, Object> record : records) {
            result.add(String.valueOf(record.get("id")));
        }
        return result;
    }
}
