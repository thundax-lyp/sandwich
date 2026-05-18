package com.github.thundax.integration.storage;

import static org.junit.Assert.assertArrayEquals;
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

public class AdminStorageObjectMutationIT extends AbstractAdminApiIT {

    private AuthToken token;

    @Before
    public void prepareData() {
        prepareIntegrationData();
        token = loginAdmin();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldUploadReadSortAndDeleteStorageObject() {
        byte[] content = "integration storage upload".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Map<String, Object> uploaded = dataMap(httpClient.postMultipart(
                "/api/storage/object/upload",
                httpClient.multipartBody(null, "file", "integration-storage.txt", content, MediaType.TEXT_PLAIN),
                authHeaders(token.getToken()),
                Map.class));
        String uploadedId = String.valueOf(uploaded.get("id"));
        assertNotNull(uploadedId);
        assertEquals("integration-storage.txt", uploaded.get("originalFilename"));
        assertEquals("text/plain", uploaded.get("contentType"));

        byte[] stored = httpClient.get(
                String.valueOf(uploaded.get("contentUrl")).replace("/admin-api", ""),
                authHeaders(token.getToken()),
                byte[].class);
        assertArrayEquals(content, stored);

        List<Map<String, Object>> records = (List<Map<String, Object>>) dataMap(httpClient.postJson(
                        "/api/storage/object/page", pageRequest(), authHeaders(token.getToken()), Map.class))
                .get("records");
        List<String> ids = ids(records);
        Collections.reverse(ids);
        assertEquals(
                Boolean.TRUE,
                data(httpClient.postJson(
                        "/api/storage/object/sort", sortRequest(ids), authHeaders(token.getToken()), Map.class)));

        assertEquals(
                Boolean.TRUE,
                data(httpClient.postJson(
                        "/api/storage/object/delete",
                        Collections.singletonList(request("id", uploadedId)),
                        authHeaders(token.getToken()),
                        Map.class)));
        assertContentNotFound(uploadedId);
    }

    @Test
    public void shouldRejectReadonlyUserWhenUploadingStorageObject() {
        AuthToken readonlyToken = loginAdmin("it-user", "Q1w2e3r$", "8888");
        try {
            httpClient.postMultipart(
                    "/api/storage/object/upload",
                    httpClient.multipartBody(
                            null, "file", "integration-forbidden.txt", new byte[] {1}, MediaType.TEXT_PLAIN),
                    authHeaders(readonlyToken.getToken()),
                    Map.class);
        } catch (HttpClientErrorException expected) {
            assertEquals(HttpStatus.FORBIDDEN, expected.getStatusCode());
            return;
        }
        fail("readonly user should not upload storage object");
    }

    private Map<String, Object> pageRequest() {
        Map<String, Object> request = request("pageNo", 1);
        request.put("pageSize", 20);
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

    private void assertContentNotFound(String id) {
        try {
            httpClient.get("/api/storage/object/" + id + "/content", authHeaders(token.getToken()), byte[].class);
        } catch (HttpClientErrorException expected) {
            assertEquals(HttpStatus.NOT_FOUND, expected.getStatusCode());
            return;
        }
        fail("deleted storage content should not be readable");
    }
}
