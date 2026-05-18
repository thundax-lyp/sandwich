package com.github.thundax.integration;

import com.github.thundax.OpenApiApplication;
import com.github.thundax.common.test.integration.IntegrationDatabaseScriptRunner;
import com.github.thundax.common.test.integration.IntegrationHttpClient;
import com.github.thundax.common.test.integration.IntegrationOssCleaner;
import com.github.thundax.common.test.integration.IntegrationRedisCleaner;
import com.github.thundax.common.test.integration.IntegrationTestProfileGuard;
import com.github.thundax.modules.auth.security.OpenApiCanonicalRequest;
import com.github.thundax.modules.auth.security.OpenApiHeaders;
import com.github.thundax.modules.auth.security.OpenApiSignatureVerifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
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
@SpringBootTest(classes = OpenApiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public abstract class AbstractOpenApiIT {

    @Autowired
    protected Environment environment;

    @Autowired
    protected DataSource dataSource;

    @Autowired
    protected RedisConnectionFactory redisConnectionFactory;

    @Value("${server.port}")
    protected int serverPort;

    @Value("${server.servlet.context-path:/open-api}")
    protected String contextPath;

    @Value("${sandwish.integration-test.redis.key-prefix:sandwish:it:}")
    protected String redisKeyPrefix;

    @Value("${sandwish.oss.local.root-path:target/integration-test/oss}")
    protected String ossRootPath;

    @Value("${sandwish.integration-test.open-api.client-id:it-open-client}")
    protected String openClientId;

    @Value("${sandwish.integration-test.open-api.client-secret:it-open-secret}")
    protected String openClientSecret;

    protected IntegrationHttpClient httpClient;
    protected OpenApiSignatureVerifier signatureVerifier;

    @Before
    public void setUpOpenApiIntegrationTest() {
        IntegrationTestProfileGuard.assertEnabled(environment);
        httpClient = new IntegrationHttpClient(baseUrl());
        signatureVerifier = new OpenApiSignatureVerifier();
    }

    protected String baseUrl() {
        return "http://127.0.0.1:" + serverPort + contextPath;
    }

    protected Map<String, String> signedHeaders(String method, String path, String queryString, String body) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonce = "it-" + timestamp;
        String contentSha256 =
                signatureVerifier.sha256Hex(body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8));
        OpenApiCanonicalRequest canonicalRequest =
                new OpenApiCanonicalRequest(method, contextPath + path, queryString, timestamp, nonce, contentSha256);
        Map<String, String> headers = new LinkedHashMap<String, String>();
        headers.put(OpenApiHeaders.API_KEY, openClientId);
        headers.put(OpenApiHeaders.TIMESTAMP, timestamp);
        headers.put(OpenApiHeaders.NONCE, nonce);
        headers.put(OpenApiHeaders.CONTENT_SHA256, contentSha256);
        headers.put(
                OpenApiHeaders.SIGNATURE, signatureVerifier.hmacSha256Hex(openClientSecret, canonicalRequest.value()));
        return headers;
    }

    protected void runSql(Path... directories) {
        new IntegrationDatabaseScriptRunner(dataSource).runDirectories(directories);
    }

    protected void cleanRedis() {
        new IntegrationRedisCleaner(redisConnectionFactory).cleanByPrefix(redisKeyPrefix);
    }

    protected void cleanOss() {
        new IntegrationOssCleaner(Paths.get(ossRootPath)).clean();
    }
}
