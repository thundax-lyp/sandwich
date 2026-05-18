package com.github.thundax.integration.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.github.thundax.common.test.integration.IntegrationAuthClient.AuthToken;
import com.github.thundax.integration.AbstractAdminApiIT;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;

public class AdminMultipartUploadIT extends AbstractAdminApiIT {

    private AuthToken token;

    @Before
    public void prepareData() {
        prepareIntegrationData();
        token = loginAdmin();
    }

    @Test
    public void shouldInitUploadPartsCompleteAndAbortMultipartUpload() {
        Map<String, Object> session = dataMap(httpClient.postJson(
                "/api/storage/multipart-upload",
                initRequest("integration-multipart.txt", 10L, 5L),
                authHeaders(token.getToken()),
                Map.class));
        String uploadId = String.valueOf(session.get("uploadId"));
        assertNotNull(uploadId);
        assertEquals("INITIATED", session.get("uploadStatus"));
        assertEquals(0, ((Number) session.get("uploadedPartCount")).intValue());

        Map<String, Object> part1 = dataMap(httpClient.postMultipart(
                "/api/storage/multipart-upload/" + uploadId + "/parts?partNumber=1&etag=part-one",
                httpClient.multipartBody(null, "file", "part-1.txt", "hello".getBytes(), MediaType.TEXT_PLAIN),
                authHeaders(token.getToken()),
                Map.class));
        assertEquals(uploadId, part1.get("uploadId"));
        assertEquals(1, ((Number) part1.get("partNumber")).intValue());

        Map<String, Object> part2 = dataMap(httpClient.postMultipart(
                "/api/storage/multipart-upload/" + uploadId + "/parts?partNumber=2&etag=part-two",
                httpClient.multipartBody(null, "file", "part-2.txt", "world".getBytes(), MediaType.TEXT_PLAIN),
                authHeaders(token.getToken()),
                Map.class));
        assertEquals(2, ((Number) part2.get("partNumber")).intValue());

        Map<String, Object> completed = dataMap(httpClient.postJson(
                "/api/storage/multipart-upload/" + uploadId + "/complete",
                completeRequest(10L),
                authHeaders(token.getToken()),
                Map.class));
        assertNotNull(completed.get("id"));
        assertEquals("integration-multipart.txt", completed.get("originalFilename"));
        assertEquals("UNREFERENCED", completed.get("referenceStatus"));

        Map<String, Object> abortSession = dataMap(httpClient.postJson(
                "/api/storage/multipart-upload",
                initRequest("integration-abort.txt", 5L, 5L),
                authHeaders(token.getToken()),
                Map.class));
        assertEquals(
                Boolean.TRUE,
                data(httpClient.postJson(
                        "/api/storage/multipart-upload/" + abortSession.get("uploadId") + "/abort",
                        request("unused", true),
                        authHeaders(token.getToken()),
                        Map.class)));
    }

    @Test
    public void shouldRejectReadonlyUserWhenInitializingMultipartUpload() {
        AuthToken readonlyToken = loginAdmin("it-user", "Q1w2e3r$", "8888");
        try {
            httpClient.postJson(
                    "/api/storage/multipart-upload",
                    initRequest("integration-forbidden.txt", 1L, 1L),
                    authHeaders(readonlyToken.getToken()),
                    Map.class);
        } catch (HttpClientErrorException expected) {
            assertEquals(HttpStatus.FORBIDDEN, expected.getStatusCode());
            return;
        }
        fail("readonly user should not initialize multipart upload");
    }

    private Map<String, Object> initRequest(String filename, Long totalSize, Long partSize) {
        Map<String, Object> request = request("businessType", "integration");
        request.put("originalFilename", filename);
        request.put("mimeType", "text/plain");
        request.put("totalSize", totalSize);
        request.put("partSize", partSize);
        return request;
    }

    private Map<String, Object> completeRequest(Long size) {
        Map<String, Object> request = request("bucketName", "sandwish-it");
        request.put("objectKey", "integration/multipart/completed.txt");
        request.put("size", size);
        request.put("accessEndpoint", "/api/storage/object/" + System.nanoTime() + "/content");
        return request;
    }
}
