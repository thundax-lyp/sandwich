package com.github.thundax.integration;

import com.github.thundax.FrontApiApplication;
import com.github.thundax.common.test.integration.IntegrationAuthClient;
import com.github.thundax.common.test.integration.IntegrationDatabaseScriptRunner;
import com.github.thundax.common.test.integration.IntegrationHttpClient;
import com.github.thundax.common.test.integration.IntegrationOssCleaner;
import com.github.thundax.common.test.integration.IntegrationRedisCleaner;
import com.github.thundax.common.test.integration.IntegrationTestProfileGuard;
import com.github.thundax.modules.auth.entity.PreAuthSession;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionId;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionToken;
import com.github.thundax.modules.auth.service.PreAuthSessionService;
import com.github.thundax.modules.auth.service.command.UpsertPreAuthSessionValueCommand;
import com.github.thundax.modules.auth.service.query.PreAuthSessionValueQuery;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.sql.DataSource;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles("it")
@SpringBootTest(classes = FrontApiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public abstract class AbstractFrontApiIT {

    private static final String SANDWISH_CACHE_PREFIX = "_SANDWISH_";

    @Autowired
    protected Environment environment;

    @Autowired
    protected DataSource dataSource;

    @Autowired
    protected RedisConnectionFactory redisConnectionFactory;

    @Autowired
    protected PreAuthSessionService preAuthSessionService;

    @Value("${server.port}")
    protected int serverPort;

    @Value("${server.servlet.context-path:/front-api}")
    protected String contextPath;

    @Value("${sandwish.integration-test.redis.key-prefix:sandwish:it:}")
    protected String redisKeyPrefix;

    @Value("${sandwish.oss.local.root-path:target/integration-test/oss}")
    protected String ossRootPath;

    protected IntegrationHttpClient httpClient;
    protected IntegrationAuthClient authClient;

    @Before
    public void setUpFrontApiIntegrationTest() {
        IntegrationTestProfileGuard.assertEnabled(environment);
        httpClient = new IntegrationHttpClient(baseUrl());
        authClient = new IntegrationAuthClient(httpClient);
    }

    protected String baseUrl() {
        return "http://127.0.0.1:" + serverPort + contextPath;
    }

    protected void runSql(Path... directories) {
        new IntegrationDatabaseScriptRunner(dataSource).runDirectories(directories);
    }

    protected void cleanRedis() {
        IntegrationRedisCleaner cleaner = new IntegrationRedisCleaner(redisConnectionFactory);
        cleaner.cleanByPrefix(redisKeyPrefix);
        cleaner.cleanByPrefix(SANDWISH_CACHE_PREFIX);
    }

    protected void cleanOss() {
        new IntegrationOssCleaner(Paths.get(ossRootPath)).clean();
    }

    protected void prepareIntegrationData() {
        cleanRedis();
        cleanOss();
        runSql(
                Paths.get("db/schema"),
                Paths.get("deploy/integration/db/90-cleanup"),
                Paths.get("deploy/integration/db/10-baseline"));
    }

    protected Map<String, String> authHeaders(String accessToken) {
        return Collections.singletonMap("Authorization", "Bearer " + accessToken);
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> dataMap(Map<?, ?> response) {
        if (response == null) {
            return new LinkedHashMap<String, Object>();
        }
        if (!response.containsKey("data")) {
            return (Map<String, Object>) response;
        }
        Object data = response.get("data");
        return data == null ? new LinkedHashMap<String, Object>() : (Map<String, Object>) data;
    }

    protected Object data(Map<?, ?> response) {
        if (response == null) {
            return null;
        }
        return response.containsKey("data") ? response.get("data") : response;
    }

    protected Map<String, Object> request(String name, Object value) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put(name, value);
        return request;
    }

    protected Map<String, Object> createPreAuthSession() {
        return dataMap(httpClient.postJson(
                "/api/auth/session/pre-auth-session", new LinkedHashMap<String, Object>(), Map.class));
    }

    protected String preAuthValue(String loginToken, String name) {
        PreAuthSessionId sessionId = preAuthSessionService.getIdByToken(PreAuthSessionToken.of(loginToken));
        return preAuthSessionService.getValue(new PreAuthSessionValueQuery(sessionId, name));
    }

    protected void upsertPreAuthValue(String loginToken, String name, String value) {
        PreAuthSessionId sessionId = preAuthSessionService.getIdByToken(PreAuthSessionToken.of(loginToken));
        PreAuthSession session = preAuthSessionService.get(sessionId);
        preAuthSessionService.upsertValue(
                new UpsertPreAuthSessionValueCommand(sessionId, name, value, session.getExpiredAt()));
    }

    protected String encryptRsa(String plainText, String publicKeyText) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyText);
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("RSA encryption failed", e);
        }
    }
}
