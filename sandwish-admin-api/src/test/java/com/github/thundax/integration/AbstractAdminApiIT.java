package com.github.thundax.integration;

import com.github.thundax.AdminApiApplication;
import com.github.thundax.common.test.integration.IntegrationAuthClient;
import com.github.thundax.common.test.integration.IntegrationDatabaseScriptRunner;
import com.github.thundax.common.test.integration.IntegrationHttpClient;
import com.github.thundax.common.test.integration.IntegrationOssCleaner;
import com.github.thundax.common.test.integration.IntegrationRedisCleaner;
import com.github.thundax.common.test.integration.IntegrationTestProfileGuard;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        new IntegrationRedisCleaner(redisConnectionFactory).cleanByPrefix(redisKeyPrefix);
    }

    protected void cleanOss() {
        new IntegrationOssCleaner(Paths.get(ossRootPath)).clean();
    }
}
