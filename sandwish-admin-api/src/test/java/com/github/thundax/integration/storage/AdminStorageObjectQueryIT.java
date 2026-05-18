package com.github.thundax.integration.storage;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.github.thundax.common.test.integration.IntegrationAuthClient.AuthToken;
import com.github.thundax.integration.AbstractAdminApiIT;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class AdminStorageObjectQueryIT extends AbstractAdminApiIT {

    private static final String SUBMISSION_IMAGE_ID = "9100000000000070001";

    private AuthToken token;

    @Before
    public void prepareData() {
        prepareIntegrationData();
        token = loginAdmin();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldPageStorageObjectsAndListReferenceTree() {
        Map<String, Object> pageRequest = request("pageNo", 1);
        pageRequest.put("pageSize", 10);
        pageRequest.put("contentType", "image/jpeg");
        pageRequest.put("objectStatus", "ACTIVE");
        pageRequest.put("referenceStatus", "REFERENCED");
        pageRequest.put("originalFilename", "submission-image");
        Map<String, Object> page = dataMap(
                httpClient.postJson("/api/storage/object/page", pageRequest, authHeaders(token.getToken()), Map.class));
        assertTrue(((Number) page.get("count")).longValue() >= 1L);
        assertTrue(containsById((List<Map<String, Object>>) page.get("records"), SUBMISSION_IMAGE_ID));

        List<Map<String, Object>> tree = (List<Map<String, Object>>) data(httpClient.postJson(
                "/api/storage/object/tree", request("unused", true), authHeaders(token.getToken()), Map.class));
        assertNotNull(tree);
        assertTrue(containsNode(tree, "SUBMISSION"));
    }

    @Test
    public void shouldAllowReadonlyUserWhenViewingStorageObjects() {
        AuthToken readonlyToken = loginAdmin("it-user", "Q1w2e3r$", "8888");
        Map<String, Object> page = dataMap(httpClient.postJson(
                "/api/storage/object/page", request("pageNo", 1), authHeaders(readonlyToken.getToken()), Map.class));
        assertTrue(((Number) page.get("count")).longValue() >= 1L);
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

    private boolean containsNode(List<Map<String, Object>> records, String id) {
        for (Map<String, Object> record : records) {
            if (id.equals(String.valueOf(record.get("id")))) {
                return true;
            }
        }
        return false;
    }
}
