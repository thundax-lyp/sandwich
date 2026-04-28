package com.github.thundax.architecture;

import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.Test;

public class PersistenceMigrationArchitectureTest {

    private static final List<String> MODULES =
            Arrays.asList(
                    "sandwish-common",
                    "sandwish-biz",
                    "sandwish-infra",
                    "sandwish-admin-api",
                    "sandwish-front-api");

    @Test
    public void shouldNotKeepMapperXmlInMainSources() throws IOException {
        Path root = repositoryRoot();
        for (String module : MODULES) {
            Path main = root.resolve(module).resolve("src/main");
            if (!Files.exists(main)) {
                continue;
            }
            try (Stream<Path> paths = Files.walk(main)) {
                assertFalse(
                        "Mapper XML must not exist in main sources",
                        paths.anyMatch(
                                path ->
                                        path.toString().contains("/mapper/mapping/")
                                                && path.toString().endsWith(".xml")));
            }
        }
    }

    @Test
    public void shouldNotDependOnPageHelperOrOldCrudInfrastructure() throws IOException {
        Path root = repositoryRoot();
        for (String module : MODULES) {
            Path modulePath = root.resolve(module);
            if (!Files.exists(modulePath)) {
                continue;
            }
            Path pom = modulePath.resolve("pom.xml");
            if (Files.exists(pom)) {
                assertFalse(
                        "PageHelper and old CRUD infrastructure must not be used",
                        containsForbiddenPersistenceToken(pom));
            }
            Path main = modulePath.resolve("src/main");
            if (!Files.exists(main)) {
                continue;
            }
            try (Stream<Path> paths = Files.walk(main)) {
                assertFalse(
                        "PageHelper and old CRUD infrastructure must not be used",
                        paths.filter(Files::isRegularFile)
                                .filter(this::isProjectSource)
                                .anyMatch(this::containsForbiddenPersistenceToken));
            }
        }
    }

    private boolean isProjectSource(Path path) {
        String value = path.toString();
        return value.endsWith(".java") || value.endsWith(".xml") || value.endsWith("pom.xml");
    }

    private boolean containsForbiddenPersistenceToken(Path path) {
        try {
            String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            return content.contains("PageHelper")
                    || content.contains("com.github.pagehelper")
                    || content.contains("pagehelper-spring-boot-starter")
                    || content.contains("CrudDao")
                    || content.contains("CrudServiceImpl")
                    || content.contains("MyBatisDao");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private Path repositoryRoot() {
        Path current = Paths.get("").toAbsolutePath();
        if (Files.exists(current.resolve("TODO.md"))) {
            return current;
        }
        Path parent = current.getParent();
        while (parent != null) {
            if (Files.exists(parent.resolve("TODO.md"))) {
                return parent;
            }
            parent = parent.getParent();
        }
        throw new IllegalStateException("Can not find repository root");
    }
}
