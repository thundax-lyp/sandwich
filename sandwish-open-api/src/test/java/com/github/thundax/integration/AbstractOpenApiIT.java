package com.github.thundax.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.thundax.OpenApiApplication;
import com.github.thundax.common.test.integration.IntegrationDatabaseScriptRunner;
import com.github.thundax.common.test.integration.IntegrationHttpClient;
import com.github.thundax.common.test.integration.IntegrationOssCleaner;
import com.github.thundax.common.test.integration.IntegrationRedisCleaner;
import com.github.thundax.common.test.integration.IntegrationTestProfileGuard;
import com.github.thundax.modules.auth.security.OpenApiCanonicalRequest;
import com.github.thundax.modules.auth.security.OpenApiHeaders;
import com.github.thundax.modules.auth.security.OpenApiSignatureVerifier;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@ActiveProfiles("it")
@SpringBootTest(classes = OpenApiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public abstract class AbstractOpenApiIT {

    private static final String SANDWISH_CACHE_PREFIX = "_SANDWISH_";
    private static final String OPEN_API_NONCE_PREFIX = "open-api:nonce:";

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

    @Autowired
    protected ObjectMapper objectMapper;

    protected IntegrationHttpClient httpClient;
    protected OpenApiSignatureVerifier signatureVerifier;
    protected RestTemplate restTemplate;

    @Before
    public void setUpOpenApiIntegrationTest() {
        IntegrationTestProfileGuard.assertEnabled(environment);
        httpClient = new IntegrationHttpClient(baseUrl());
        signatureVerifier = new OpenApiSignatureVerifier();
        restTemplate = new RestTemplate();
    }

    protected String baseUrl() {
        return "http://127.0.0.1:" + serverPort + contextPath;
    }

    protected Map<String, String> signedHeaders(String method, String path, String queryString, String body) {
        return signedHeaders(method, path, queryString, body, "it-" + System.currentTimeMillis());
    }

    protected Map<String, String> signedHeaders(
            String method, String path, String queryString, String body, String nonce) {
        return signedHeaders(
                method,
                path,
                queryString,
                body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8),
                nonce,
                openClientId,
                openClientSecret);
    }

    protected Map<String, String> signedHeaders(
            String method, String path, String queryString, byte[] body, String nonce) {
        return signedHeaders(method, path, queryString, body, nonce, openClientId, openClientSecret);
    }

    protected Map<String, String> signedHeaders(
            String method, String path, String queryString, byte[] body, String nonce, String apiKey, String secret) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String contentSha256 = signatureVerifier.sha256Hex(body == null ? new byte[0] : body);
        OpenApiCanonicalRequest canonicalRequest =
                new OpenApiCanonicalRequest(method, contextPath + path, queryString, timestamp, nonce, contentSha256);
        Map<String, String> headers = new LinkedHashMap<String, String>();
        headers.put(OpenApiHeaders.API_KEY, apiKey);
        headers.put(OpenApiHeaders.TIMESTAMP, timestamp);
        headers.put(OpenApiHeaders.NONCE, nonce);
        headers.put(OpenApiHeaders.CONTENT_SHA256, contentSha256);
        headers.put(OpenApiHeaders.SIGNATURE, signatureVerifier.hmacSha256Hex(secret, canonicalRequest.value()));
        return headers;
    }

    protected void runSql(Path... directories) {
        new IntegrationDatabaseScriptRunner(dataSource).runDirectories(directories);
    }

    protected void cleanRedis() {
        IntegrationRedisCleaner cleaner = new IntegrationRedisCleaner(redisConnectionFactory);
        cleaner.cleanByPrefix(redisKeyPrefix);
        cleaner.cleanByPrefix(SANDWISH_CACHE_PREFIX);
        cleaner.cleanByPrefix(OPEN_API_NONCE_PREFIX);
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

    protected <T> T postSignedJson(String path, Map<String, Object> request, Class<T> responseType) {
        return postSignedJson(path, request, signedHeaders("POST", path, "", json(request)), responseType);
    }

    protected <T> T postSignedJson(
            String path, Map<String, Object> request, Map<String, String> headers, Class<T> responseType) {
        return httpClient.postJson(path, json(request), headers, responseType);
    }

    protected <T> T postSignedMultipart(
            String path,
            String fileName,
            byte[] content,
            MediaType fileContentType,
            Map<String, String> headers,
            Class<T> responseType) {
        String boundary = "----SandwishIntegrationBoundary" + System.nanoTime();
        byte[] body = multipartBody(boundary, "file", fileName, content, fileContentType);
        Map<String, String> signedHeaders =
                headers == null ? signedHeaders("POST", path, "", body, "it-" + System.currentTimeMillis()) : headers;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.parseMediaType("multipart/form-data; boundary=" + boundary));
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        for (Map.Entry<String, String> entry : signedHeaders.entrySet()) {
            httpHeaders.set(entry.getKey(), entry.getValue());
        }
        return restTemplate.postForObject(baseUrl() + path, new HttpEntity<byte[]>(body, httpHeaders), responseType);
    }

    protected String json(Map<String, Object> request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize integration request", e);
        }
    }

    protected byte[] multipartBody(
            String boundary, String fileField, String fileName, byte[] content, MediaType contentType) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            outputStream.write(
                    ("Content-Disposition: form-data; name=\"" + fileField + "\"; filename=\"" + fileName + "\"\r\n")
                            .getBytes(StandardCharsets.UTF_8));
            outputStream.write(
                    ("Content-Type: " + contentType.toString() + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            outputStream.write(content);
            outputStream.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to build multipart integration request", e);
        }
    }
}
