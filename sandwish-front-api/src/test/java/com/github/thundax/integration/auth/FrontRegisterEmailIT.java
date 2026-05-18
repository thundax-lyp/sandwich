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

public class FrontRegisterEmailIT extends AbstractFrontApiIT {

    @Before
    public void prepareData() {
        prepareIntegrationData();
    }

    @Test
    public void shouldSendCodeRegisterEmailAndRejectDuplicateEmail() {
        Map<String, Object> registered = registerEmail("integration-front-email@sandwish.local");
        assertEquals(Boolean.TRUE, registered.get("success"));
        assertNotNull(registered.get("memberId"));

        try {
            registerEmail("integration-front-email@sandwish.local");
        } catch (HttpClientErrorException expected) {
            assertEquals(HttpStatus.CONFLICT, expected.getStatusCode());
            return;
        }
        fail("duplicate email should be rejected");
    }

    private Map<String, Object> registerEmail(String email) {
        Map<String, Object> session = createPreAuthSession();
        String loginToken = String.valueOf(session.get("loginToken"));
        Map<String, Object> codeRequest = request("loginToken", loginToken);
        codeRequest.put("email", email);
        codeRequest.put("captcha", "6666");
        Map<String, Object> codeResponse =
                dataMap(httpClient.postJson("/api/auth/register/email/code", codeRequest, Map.class));
        assertEquals(Boolean.TRUE, codeResponse.get("success"));

        Map<String, Object> request = request("loginToken", loginToken);
        request.put("name", "Integration Email Member");
        request.put("email", email);
        request.put("validateCode", preAuthValue(loginToken, "EMAIL_VALIDATE_CODE"));
        return dataMap(httpClient.postJson("/api/auth/register/email", request, Map.class));
    }
}
