package com.github.thundax.integration.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.github.thundax.common.crypto.Sm2Crypto;
import com.github.thundax.common.test.integration.IntegrationAuthClient.AuthToken;
import com.github.thundax.common.test.integration.IntegrationAuthClient.PreAuthSession;
import com.github.thundax.integration.AbstractAdminApiIT;
import java.util.Collections;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

public class AdminAuthSessionIT extends AbstractAdminApiIT {

    @Before
    public void prepareData() {
        prepareIntegrationData();
    }

    @Test
    public void shouldLoginRefreshVerifyAndLogoutWithWhitelistedCaptcha() {
        PreAuthSession preAuthSession = authClient.createAdminPreAuthSession();

        AuthToken token = login(preAuthSession, "it-admin", "Q1w2e3r$", "6666");

        assertNotNull(token.getToken());
        assertNotNull(token.getRefreshToken());
        assertTrue(token.getExpireAt() > System.currentTimeMillis());

        Map<String, Object> verifyResponse = dataMap(
                httpClient.postJson("/api/auth/session/token/verify", request("token", token.getToken()), Map.class));
        assertEquals(Boolean.TRUE, verifyResponse.get("active"));

        AuthToken refreshedToken = authClient.refreshAdminToken(token.getRefreshToken());
        assertNotNull(refreshedToken.getToken());
        assertNotEquals(token.getToken(), refreshedToken.getToken());

        assertEquals(Boolean.TRUE, authClient.logoutAdmin(refreshedToken.getToken()));
    }

    @Test
    public void shouldRejectNormalUserWhenCallingSuperEndpoint() {
        PreAuthSession preAuthSession = authClient.createAdminPreAuthSession();
        AuthToken token = login(preAuthSession, "it-user", "Q1w2e3r$", "8888");

        try {
            httpClient.postJson("/api/sys/menu/list", Collections.emptyMap(), authHeaders(token.getToken()), Map.class);
        } catch (HttpClientErrorException expected) {
            assertEquals(HttpStatus.FORBIDDEN, expected.getStatusCode());
            return;
        }
        fail("normal integration user should not access super endpoint");
    }

    private AuthToken login(PreAuthSession preAuthSession, String userName, String password, String captcha) {
        return authClient.loginAdmin(
                preAuthSession.getLoginToken(),
                userName,
                Sm2Crypto.encrypt(password, preAuthSession.getPublicKey()),
                captcha);
    }
}
