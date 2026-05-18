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

public class FrontRegisterMobileIT extends AbstractFrontApiIT {

    @Before
    public void prepareData() {
        prepareIntegrationData();
    }

    @Test
    public void shouldSendCodeRegisterMobileAndRejectDuplicateMobile() {
        Map<String, Object> registered = registerMobile("it-front-mobile-register");
        assertEquals(Boolean.TRUE, registered.get("success"));
        assertNotNull(registered.get("memberId"));

        try {
            registerMobile("it-front-mobile-register");
        } catch (HttpClientErrorException expected) {
            assertEquals(HttpStatus.CONFLICT, expected.getStatusCode());
            return;
        }
        fail("duplicate mobile should be rejected");
    }

    private Map<String, Object> registerMobile(String mobile) {
        Map<String, Object> session = createPreAuthSession();
        String loginToken = String.valueOf(session.get("loginToken"));
        Map<String, Object> codeRequest = request("loginToken", loginToken);
        codeRequest.put("mobile", mobile);
        codeRequest.put("captcha", "6666");
        Map<String, Object> codeResponse =
                dataMap(httpClient.postJson("/api/auth/register/mobile/code", codeRequest, Map.class));
        assertEquals(Boolean.TRUE, codeResponse.get("success"));

        Map<String, Object> request = request("loginToken", loginToken);
        request.put("name", "Integration Mobile Member");
        request.put("mobile", mobile);
        request.put("validateCode", preAuthValue(loginToken, "SMS_VALIDATE_CODE"));
        return dataMap(httpClient.postJson("/api/auth/register/mobile", request, Map.class));
    }
}
