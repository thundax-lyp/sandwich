package com.github.thundax.common.test.integration;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

public class IntegrationAuthClientTest {

    private MockRestServiceServer server;
    private IntegrationAuthClient authClient;

    @Before
    public void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        authClient =
                new IntegrationAuthClient(new IntegrationHttpClient("http://127.0.0.1:12009/admin-api", restTemplate));
    }

    @Test
    public void shouldCreateAdminPreAuthSession() {
        server.expect(requestTo("http://127.0.0.1:12009/admin-api/api/auth/session/pre-auth-session"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        "{\"data\":{\"loginToken\":\"lt\",\"refreshToken\":\"rt\",\"publicKey\":\"pk\"}}",
                        MediaType.APPLICATION_JSON));

        IntegrationAuthClient.PreAuthSession session = authClient.createAdminPreAuthSession();

        assertEquals("lt", session.getLoginToken());
        assertEquals("rt", session.getRefreshToken());
        assertEquals("pk", session.getPublicKey());
        server.verify();
    }

    @Test
    public void shouldLoginAdminAndParseToken() {
        server.expect(requestTo("http://127.0.0.1:12009/admin-api/api/auth/session/login"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(
                        content()
                                .json(
                                        "{\"loginToken\":\"lt\",\"userName\":\"admin\",\"password\":\"pwd\",\"captcha\":\"6666\"}"))
                .andRespond(withSuccess(
                        "{\"data\":{\"token\":\"access\",\"refreshToken\":\"refresh\",\"expireAt\":1778513052155}}",
                        MediaType.APPLICATION_JSON));

        IntegrationAuthClient.AuthToken token = authClient.loginAdmin("lt", "admin", "pwd", "6666");

        assertEquals("access", token.getToken());
        assertEquals("refresh", token.getRefreshToken());
        assertEquals(Long.valueOf(1778513052155L), token.getExpireAt());
        server.verify();
    }
}
