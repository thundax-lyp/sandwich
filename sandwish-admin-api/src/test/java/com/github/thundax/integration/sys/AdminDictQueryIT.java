package com.github.thundax.integration.sys;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.github.thundax.common.test.integration.IntegrationAuthClient.AuthToken;
import com.github.thundax.integration.AbstractAdminApiIT;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class AdminDictQueryIT extends AbstractAdminApiIT {

    private static final String ENABLED_DICT_ID = "9100000000000000201";
    private static final String DISABLED_DICT_ID = "9100000000000000202";
    private static final String REGRESSION_DICT_ID = "9100000000000000203";

    private AuthToken token;

    @Before
    public void prepareData() {
        prepareIntegrationData();
        token = loginAdmin();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReadDictGetListAndPage() {
        Map<String, Object> dict = dataMap(httpClient.postJson(
                "/api/sys/dict/get", request("id", ENABLED_DICT_ID), authHeaders(token.getToken()), Map.class));
        assertEquals("it.status", dict.get("type"));
        assertEquals("Enabled", dict.get("label"));
        assertEquals("ENABLED", dict.get("value"));

        List<Map<String, Object>> statusDicts = (List<Map<String, Object>>) data(httpClient.postJson(
                "/api/sys/dict/list", request("type", "it.status"), authHeaders(token.getToken()), Map.class));
        assertTrue(containsById(statusDicts, ENABLED_DICT_ID));
        assertTrue(containsById(statusDicts, DISABLED_DICT_ID));

        Map<String, Object> pageQuery = request("pageNo", 1);
        pageQuery.put("pageSize", 10);
        pageQuery.put("type", "it.category");
        Map<String, Object> page =
                dataMap(httpClient.postJson("/api/sys/dict/page", pageQuery, authHeaders(token.getToken()), Map.class));
        assertTrue(((Number) page.get("count")).longValue() >= 2L);
        assertTrue(containsById((List<Map<String, Object>>) page.get("records"), REGRESSION_DICT_ID));
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
}
