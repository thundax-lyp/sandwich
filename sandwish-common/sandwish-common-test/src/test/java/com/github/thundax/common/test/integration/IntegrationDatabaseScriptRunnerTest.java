package com.github.thundax.common.test.integration;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class IntegrationDatabaseScriptRunnerTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void shouldRunSqlScriptsInNameOrder() throws Exception {
        Path directory = temporaryFolder.newFolder("db").toPath();
        Path second = directory.resolve("002-second.sql");
        Path first = directory.resolve("001-first.sql");
        Files.write(second, "select 2;".getBytes(StandardCharsets.UTF_8));
        Files.write(first, "select 1;".getBytes(StandardCharsets.UTF_8));
        Files.write(directory.resolve("README.md"), "ignored".getBytes(StandardCharsets.UTF_8));
        List<Path> executed = new ArrayList<Path>();
        IntegrationDatabaseScriptRunner runner = new IntegrationDatabaseScriptRunner(executed::add);

        List<Path> scripts = runner.runDirectory(directory);

        assertEquals(first, scripts.get(0));
        assertEquals(second, scripts.get(1));
        assertEquals(scripts, executed);
    }

    @Test
    public void shouldIgnoreMissingDirectory() {
        IntegrationDatabaseScriptRunner runner = new IntegrationDatabaseScriptRunner(script -> {});

        assertEquals(
                0,
                runner.runDirectory(temporaryFolder.getRoot().toPath().resolve("missing"))
                        .size());
    }
}
