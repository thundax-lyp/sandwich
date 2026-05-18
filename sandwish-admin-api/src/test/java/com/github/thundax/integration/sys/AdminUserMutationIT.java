package com.github.thundax.integration.sys;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.github.thundax.common.test.integration.IntegrationAuthClient.AuthToken;
import com.github.thundax.integration.AbstractAdminApiIT;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;

public class AdminUserMutationIT extends AbstractAdminApiIT {

    private static final String CREATED_USER_ID = "9100000000000000110";
    private static final String UPDATED_LOGIN_NAME = "it-created-user-renamed";
    private static final String QA_DEPARTMENT_ID = "9100000000000000003";
    private static final String READONLY_ROLE_ID = "9100000000000000402";

    private AuthToken token;

    @Before
    public void prepareData() {
        prepareIntegrationData();
        token = loginAdmin();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldCreateUpdateStatusAvatarAndDeleteUser() throws Exception {
        Map<String, Object> created = dataMap(httpClient.postJson(
                "/api/sys/user/create",
                userSaveRequest(CREATED_USER_ID, "it-created-user", "Integration Created User", "Plain@123"),
                authHeaders(token.getToken()),
                Map.class));
        String createdUserId = String.valueOf(created.get("id"));
        assertNotNull(createdUserId);
        assertEquals("it-created-user", created.get("loginName"));
        assertTrue(containsById((List<Map<String, Object>>) created.get("roles"), READONLY_ROLE_ID));

        Map<String, Object> updated = dataMap(httpClient.postJson(
                "/api/sys/user/update",
                userSaveRequest(createdUserId, UPDATED_LOGIN_NAME, "Integration Updated User", "Plain@456"),
                authHeaders(token.getToken()),
                Map.class));
        assertEquals(UPDATED_LOGIN_NAME, updated.get("loginName"));
        assertEquals("Integration Updated User", updated.get("name"));

        assertEquals(
                Boolean.TRUE,
                data(httpClient.postJson(
                        "/api/sys/user/enable",
                        Collections.singletonList(statusRequest(createdUserId, false)),
                        authHeaders(token.getToken()),
                        Map.class)));
        Map<String, Object> disabled = dataMap(httpClient.postJson(
                "/api/sys/user/get", request("id", createdUserId), authHeaders(token.getToken()), Map.class));
        assertEquals(Boolean.FALSE, disabled.get("enable"));

        assertEquals(
                Boolean.TRUE,
                data(httpClient.postJson(
                        "/api/sys/user/enable",
                        Collections.singletonList(statusRequest(createdUserId, true)),
                        authHeaders(token.getToken()),
                        Map.class)));

        assertEquals(
                Boolean.TRUE,
                data(httpClient.postMultipart(
                        "/api/sys/user/avatar/upload",
                        httpClient.multipartBody(
                                request("id", createdUserId),
                                "avatar",
                                "created-user.jpg",
                                jpegBytes(),
                                MediaType.IMAGE_JPEG),
                        authHeaders(token.getToken()),
                        Map.class)));
        Map<String, Object> withAvatar = dataMap(httpClient.postJson(
                "/api/sys/user/get", request("id", createdUserId), authHeaders(token.getToken()), Map.class));
        assertNotNull(withAvatar.get("avatar"));
        byte[] avatar = httpClient.get(
                String.valueOf(withAvatar.get("avatar")).replace("/admin-api", ""),
                authHeaders(token.getToken()),
                byte[].class);
        assertTrue(avatar.length > 0);

        assertEquals(
                Boolean.TRUE,
                data(httpClient.postJson(
                        "/api/sys/user/avatar/delete",
                        request("id", createdUserId),
                        authHeaders(token.getToken()),
                        Map.class)));

        assertEquals(
                Boolean.TRUE,
                data(httpClient.postJson(
                        "/api/sys/user/delete",
                        Collections.singletonList(request("id", createdUserId)),
                        authHeaders(token.getToken()),
                        Map.class)));
        assertNotFound("/api/sys/user/get", request("id", createdUserId));
    }

    @Test
    public void shouldRejectReadonlyUserWhenEditingUser() {
        AuthToken readonlyToken = loginAdmin("it-user", "Q1w2e3r$", "8888");
        try {
            httpClient.postJson(
                    "/api/sys/user/create",
                    userSaveRequest(CREATED_USER_ID, "it-forbidden-user", "Forbidden User", "Plain@123"),
                    authHeaders(readonlyToken.getToken()),
                    Map.class);
        } catch (HttpClientErrorException expected) {
            assertEquals(HttpStatus.FORBIDDEN, expected.getStatusCode());
            return;
        }
        fail("readonly user should not create admin user");
    }

    private Map<String, Object> userSaveRequest(String id, String loginName, String name, String password) {
        Map<String, Object> request = request("id", id);
        request.put("remarks", "integration user mutation");
        request.put("loginName", loginName);
        request.put("loginPass", encryptedWithNewPreAuthSession(request, password));
        request.put("ranks", 8);
        request.put("name", name);
        request.put("email", loginName + "@sandwish.local");
        request.put("mobile", "15500000110");
        request.put("admin", false);
        request.put("enable", true);
        request.put("department", request("id", QA_DEPARTMENT_ID));
        request.put("roles", Collections.singletonList(request("id", READONLY_ROLE_ID)));
        return request;
    }

    private Map<String, Object> statusRequest(String id, boolean enable) {
        Map<String, Object> request = request("id", id);
        request.put("enable", enable);
        return request;
    }

    private void assertNotFound(String path, Object request) {
        try {
            httpClient.postJson(path, request, authHeaders(token.getToken()), Map.class);
        } catch (HttpClientErrorException expected) {
            assertEquals(HttpStatus.NOT_FOUND, expected.getStatusCode());
            return;
        }
        fail("resource should not exist: " + path + " " + Arrays.asList(request));
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

    private byte[] jpegBytes() throws Exception {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, Color.BLUE.getRGB());
        image.setRGB(1, 0, Color.BLUE.getRGB());
        image.setRGB(0, 1, Color.BLUE.getRGB());
        image.setRGB(1, 1, Color.BLUE.getRGB());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", output);
        return output.toByteArray();
    }
}
