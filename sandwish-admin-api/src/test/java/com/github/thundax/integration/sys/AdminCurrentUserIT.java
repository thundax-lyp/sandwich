package com.github.thundax.integration.sys;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.github.thundax.common.crypto.Sm2Crypto;
import com.github.thundax.common.test.integration.IntegrationAuthClient.AuthToken;
import com.github.thundax.common.test.integration.IntegrationAuthClient.PreAuthSession;
import com.github.thundax.integration.AbstractAdminApiIT;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;

public class AdminCurrentUserIT extends AbstractAdminApiIT {

    private AuthToken token;

    @Before
    public void prepareData() {
        prepareIntegrationData();
        token = loginAdmin();
    }

    @Test
    public void shouldReadAndUpdateCurrentUserInfo() {
        Map<String, Object> info = dataMap(httpClient.postJson(
                "/api/sys/current-user/info", Collections.emptyMap(), authHeaders(token.getToken()), Map.class));
        assertEquals("it-admin", info.get("loginName"));
        assertEquals(Boolean.TRUE, info.get("superAdmin"));

        Map<String, Object> updateRequest = request("name", "Integration Admin Updated");
        updateRequest.put("email", "it-admin-updated@sandwish.local");
        updateRequest.put("mobile", "15500008888");
        Map<String, Object> updated = dataMap(httpClient.postJson(
                "/api/sys/current-user/info/update", updateRequest, authHeaders(token.getToken()), Map.class));
        assertEquals("Integration Admin Updated", updated.get("name"));
        assertEquals("it-admin-updated@sandwish.local", updated.get("email"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReadCurrentUserMenusAndPermissions() {
        Object menus = data(httpClient.postJson(
                "/api/sys/current-user/menus", Collections.emptyMap(), authHeaders(token.getToken()), Map.class));
        assertTrue(menus instanceof List);
        assertTrue(((List<?>) menus).size() > 0);

        Map<String, Object> perms = dataMap(httpClient.postJson(
                "/api/sys/current-user/perms", Collections.emptyMap(), authHeaders(token.getToken()), Map.class));
        assertTrue(((List<String>) perms.get("perms")).contains("super"));
        assertTrue(((List<String>) perms.get("perms")).contains("sys:user:view"));
    }

    @Test
    public void shouldUpdatePasswordWithPreAuthEncryption() {
        PreAuthSession preAuthSession = authClient.createAdminPreAuthSession();
        Map<String, Object> request =
                request("oldPassword", Sm2Crypto.encrypt("Q1w2e3r$", preAuthSession.getPublicKey()));
        request.put("password", Sm2Crypto.encrypt("Q1w2e3r$1", preAuthSession.getPublicKey()));
        request.put("token", preAuthSession.getLoginToken());

        Object updated = data(httpClient.postJson(
                "/api/sys/current-user/password/update", request, authHeaders(token.getToken()), Map.class));
        assertEquals(Boolean.TRUE, updated);
    }

    @Test
    public void shouldUploadAndDeleteAvatar() throws Exception {
        byte[] avatarBytes = jpegBytes();
        Map<String, Object> uploaded = dataMap(httpClient.postMultipart(
                "/api/sys/current-user/avatar/upload",
                httpClient.multipartBody(null, "avatar", "avatar.jpg", avatarBytes, MediaType.IMAGE_JPEG),
                authHeaders(token.getToken()),
                Map.class));
        assertNotNull(uploaded.get("avatar"));

        Map<String, Object> deleted = dataMap(httpClient.postJson(
                "/api/sys/current-user/avatar/delete",
                Collections.emptyMap(),
                authHeaders(token.getToken()),
                Map.class));
        assertEquals(null, deleted.get("avatar"));
    }

    private byte[] jpegBytes() throws Exception {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, Color.RED.getRGB());
        image.setRGB(1, 0, Color.RED.getRGB());
        image.setRGB(0, 1, Color.RED.getRGB());
        image.setRGB(1, 1, Color.RED.getRGB());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", output);
        return output.toByteArray();
    }
}
