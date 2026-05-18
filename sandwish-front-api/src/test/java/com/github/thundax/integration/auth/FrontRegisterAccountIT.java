package com.github.thundax.integration.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.github.thundax.integration.AbstractFrontApiIT;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

public class FrontRegisterAccountIT extends AbstractFrontApiIT {

    @Before
    public void prepareData() {
        prepareIntegrationData();
    }

    @Test
    public void shouldRegisterAccountAndRejectDuplicateAccount() {
        Map<String, Object> registered = registerAccount("it-front-account-register");
        assertEquals(Boolean.TRUE, registered.get("success"));
        assertNotNull(registered.get("memberId"));

        try {
            registerAccount("it-front-account-register");
        } catch (HttpClientErrorException expected) {
            assertEquals(HttpStatus.CONFLICT, expected.getStatusCode());
            return;
        }
        fail("duplicate account should be rejected");
    }

    private Map<String, Object> registerAccount(String account) {
        Map<String, Object> session = createPreAuthSession();
        Map<String, Object> request = request("loginToken", session.get("loginToken"));
        request.put("name", "Integration Account Member");
        request.put("account", account);
        request.put("password", encryptRsa("Q1w2e3r$", String.valueOf(session.get("publicKey"))));
        request.put("captcha", "6666");
        return dataMap(httpClient.postJson("/api/auth/register/account", request, Map.class));
    }
}
