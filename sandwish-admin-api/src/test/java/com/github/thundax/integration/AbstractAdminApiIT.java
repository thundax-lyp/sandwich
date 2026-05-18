package com.github.thundax.integration;

import com.github.thundax.AdminApiApplication;
import com.github.thundax.common.crypto.Sm2Crypto;
import com.github.thundax.common.test.integration.IntegrationAuthClient;
import com.github.thundax.common.test.integration.IntegrationAuthClient.AuthToken;
import com.github.thundax.common.test.integration.IntegrationAuthClient.PreAuthSession;
import com.github.thundax.common.test.integration.IntegrationDatabaseScriptRunner;
import com.github.thundax.common.test.integration.IntegrationHttpClient;
import com.github.thundax.common.test.integration.IntegrationOssCleaner;
import com.github.thundax.common.test.integration.IntegrationRedisCleaner;
import com.github.thundax.common.test.integration.IntegrationTestProfileGuard;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
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
@SpringBootTest(classes = AdminApiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public abstract class AbstractAdminApiIT {

    private static final String SANDWISH_CACHE_PREFIX = "_SANDWISH_";

    @Autowired
    protected Environment environment;

    @Autowired
    protected DataSource dataSource;

    @Autowired
    protected RedisConnectionFactory redisConnectionFactory;

    @Value("${server.port}")
    protected int serverPort;

    @Value("${server.servlet.context-path:/admin-api}")
    protected String contextPath;

    @Value("${sandwish.integration-test.redis.key-prefix:sandwish:it:}")
    protected String redisKeyPrefix;

    @Value("${sandwish.oss.local.root-path:target/integration-test/oss}")
    protected String ossRootPath;

    protected IntegrationHttpClient httpClient;
    protected IntegrationAuthClient authClient;

    @Before
    public void setUpAdminApiIntegrationTest() {
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

    protected Map<String, String> authHeaders(String token) {
        return Collections.singletonMap("Access-Token", token);
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> dataMap(Map<?, ?> response) {
        Object data = response == null ? null : response.get("data");
        return data == null ? new LinkedHashMap<String, Object>() : (Map<String, Object>) data;
    }

    protected Object data(Map<?, ?> response) {
        return response == null ? null : response.get("data");
    }

    protected Map<String, Object> request(String name, Object value) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put(name, value);
        return request;
    }

    protected AuthToken loginAdmin() {
        return loginAdmin("it-admin", "Q1w2e3r$", "6666");
    }

    protected AuthToken loginAdmin(String userName, String password, String captcha) {
        PreAuthSession preAuthSession = authClient.createAdminPreAuthSession();
        return authClient.loginAdmin(
                preAuthSession.getLoginToken(),
                userName,
                Sm2Crypto.encrypt(password, preAuthSession.getPublicKey()),
                captcha);
    }

    protected String encryptedWithNewPreAuthSession(Map<String, Object> request, String password) {
        PreAuthSession preAuthSession = authClient.createAdminPreAuthSession();
        request.put("token", preAuthSession.getLoginToken());
        return Sm2Crypto.encrypt(password, preAuthSession.getPublicKey());
    }
}
