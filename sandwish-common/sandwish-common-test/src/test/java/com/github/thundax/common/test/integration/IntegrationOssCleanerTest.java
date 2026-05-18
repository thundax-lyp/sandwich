package com.github.thundax.common.test.integration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class IntegrationOssCleanerTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void shouldCleanChildrenAndKeepRoot() throws Exception {
        Path root = temporaryFolder.newFolder("oss").toPath();
        Path nested = Files.createDirectories(root.resolve("avatar/user-1"));
        Path file = nested.resolve("a.png");
        Files.write(file, "image".getBytes(StandardCharsets.UTF_8));

        new IntegrationOssCleaner(root).clean();

        assertTrue(Files.exists(root));
        assertFalse(Files.exists(root.resolve("avatar")));
    }

    @Test
    public void shouldCreateMissingRoot() {
        Path root = temporaryFolder.getRoot().toPath().resolve("missing-oss");

        new IntegrationOssCleaner(root).clean();

        assertTrue(Files.isDirectory(root));
    }
}
