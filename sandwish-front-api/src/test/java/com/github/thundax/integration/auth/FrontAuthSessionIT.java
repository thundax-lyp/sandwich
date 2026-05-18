package com.github.thundax.integration.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import com.github.thundax.integration.AbstractFrontApiIT;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class FrontAuthSessionIT extends AbstractFrontApiIT {

    private static final String MEMBER_ID = "9100000000000060001";

    @Before
    public void prepareData() {
        prepareIntegrationData();
    }

    @Test
    public void shouldCreateAndRefreshPreAuthSession() {
        Map<String, Object> session = createPreAuthSession();
        assertNotNull(session.get("loginToken"));
        assertNotNull(session.get("refreshToken"));
        assertNotNull(session.get("publicKey"));

        Map<String, Object> refreshed = dataMap(httpClient.postJson(
                "/api/auth/session/pre-auth-session/refresh",
                request("refreshToken", session.get("refreshToken")),
                Map.class));
        assertNotNull(refreshed.get("loginToken"));
        assertNotEquals(session.get("loginToken"), refreshed.get("loginToken"));
        assertNotNull(refreshed.get("publicKey"));
    }

    @Test
    public void shouldLoginRefreshCheckStatusAndLogout() {
        Map<String, Object> login = loginByAccount("it-member", "Q1w2e3r$");
        String accessToken = String.valueOf(login.get("accessToken"));
        String refreshToken = String.valueOf(login.get("refreshToken"));
        assertEquals(MEMBER_ID, login.get("memberId"));
        assertNotNull(accessToken);
        assertNotNull(refreshToken);

        Map<String, Object> anonymousStatus = dataMap(
                httpClient.postJson("/api/auth/session/login/status", new LinkedHashMap<String, Object>(), Map.class));
        assertEquals(Boolean.FALSE, anonymousStatus.get("loggedIn"));

        Map<String, Object> loginStatus = dataMap(httpClient.postJson(
                "/api/auth/session/login/status",
                new LinkedHashMap<String, Object>(),
                authHeaders(accessToken),
                Map.class));
        assertEquals(Boolean.TRUE, loginStatus.get("loggedIn"));
        assertEquals(MEMBER_ID, loginStatus.get("memberId"));

        Map<String, Object> checkLogin = dataMap(httpClient.postJson(
                "/api/auth/session/check-login",
                new LinkedHashMap<String, Object>(),
                authHeaders(accessToken),
                Map.class));
        assertEquals(Boolean.TRUE, checkLogin.get("loggedIn"));

        Map<String, Object> refreshed = dataMap(httpClient.postJson(
                "/api/auth/session/token/refresh", request("refreshToken", refreshToken), Map.class));
        assertEquals(MEMBER_ID, refreshed.get("memberId"));
        assertNotNull(refreshed.get("accessToken"));

        Map<String, Object> logout = dataMap(httpClient.postJson(
                "/api/auth/session/logout", request("accessToken", refreshed.get("accessToken")), Map.class));
        assertEquals(Boolean.FALSE, logout.get("loggedIn"));
    }

    @Test
    public void shouldLoginWithSeededSmsCode() {
        Map<String, Object> session = createPreAuthSession();
        String loginToken = String.valueOf(session.get("loginToken"));
        upsertPreAuthValue(loginToken, "SMS_MOBILE", "15500006666");
        upsertPreAuthValue(loginToken, "SMS_VALIDATE_CODE", "246810");

        Map<String, Object> request = request("loginToken", loginToken);
        request.put("mobile", "15500006666");
        request.put("validateCode", "246810");
        Map<String, Object> login = dataMap(httpClient.postJson("/api/auth/session/login/sms", request, Map.class));
        assertEquals(MEMBER_ID, login.get("memberId"));
        assertNotNull(login.get("accessToken"));
    }

    private Map<String, Object> loginByAccount(String account, String password) {
        Map<String, Object> session = createPreAuthSession();
        Map<String, Object> request = request("loginToken", session.get("loginToken"));
        request.put("account", account);
        request.put("password", encryptRsa(password, String.valueOf(session.get("publicKey"))));
        request.put("captcha", "6666");
        return dataMap(httpClient.postJson("/api/auth/session/login", request, Map.class));
    }
}
