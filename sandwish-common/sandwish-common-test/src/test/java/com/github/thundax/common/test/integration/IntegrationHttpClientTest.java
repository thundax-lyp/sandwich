package com.github.thundax.common.test.integration;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class IntegrationHttpClientTest {

    @Test
    public void shouldPostJson() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server =
                MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("http://127.0.0.1:12009/admin-api/api/test"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("{\"name\":\"sandwich\"}"))
                .andRespond(withSuccess("{\"data\":{\"ok\":true}}", MediaType.APPLICATION_JSON));
        IntegrationHttpClient client = new IntegrationHttpClient("http://127.0.0.1:12009/admin-api", restTemplate);

        Map<?, ?> response = client.postJson("/api/test", Collections.singletonMap("name", "sandwich"), Map.class);

        assertEquals(Collections.singletonMap("ok", true), response.get("data"));
        server.verify();
    }

    @Test
    public void shouldBuildMultipartBody() {
        IntegrationHttpClient client = new IntegrationHttpClient("http://127.0.0.1:12009/admin-api");

        MultiValueMap<String, Object> body = client.multipartBody(
                Collections.singletonMap("bizType", "avatar"),
                "avatar.png",
                "image".getBytes(StandardCharsets.UTF_8),
                MediaType.IMAGE_PNG);

        assertEquals("avatar", body.getFirst("bizType"));
        assertEquals(2, body.size());
    }
}
