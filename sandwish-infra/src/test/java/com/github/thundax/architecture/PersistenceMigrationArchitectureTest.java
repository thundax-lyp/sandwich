package com.github.thundax.architecture;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.Test;

public class PersistenceMigrationArchitectureTest {

    private static final List<String> MODULES = Arrays.asList(
            "sandwish-common", "sandwish-biz", "sandwish-infra", "sandwish-admin-api", "sandwish-front-api");
    private static final Pattern DATA_OBJECT_REFERENCE_PATTERN =
            Pattern.compile("\\b[A-Z][A-Za-z0-9]*(?:DO|DataObject)\\b|\\.persistence\\.dataobject\\b");

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
                        paths.anyMatch(path -> path.toString().contains("/mapper/mapping/")
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

    @Test
    public void shouldNotFillAuditFieldsInServiceLayer() throws IOException {
        Path root = repositoryRoot();
        Path bizMain = root.resolve("sandwish-biz").resolve("src/main/java");
        try (Stream<Path> paths = Files.walk(bizMain)) {
            assertFalse(
                    "Service layer must not call preInsert/preUpdate to fill persistence audit fields",
                    paths.filter(Files::isRegularFile)
                            .filter(path -> path.toString().endsWith("ServiceImpl.java"))
                            .anyMatch(this::containsEntityPreWriteHook));
        }
    }

    @Test
    public void shouldKeepDataObjectsOnlyInInfraMainSources() throws IOException {
        Path root = repositoryRoot();
        List<Path> violations = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(this::isMainJavaSource)
                    .filter(path -> !isInfraMainJavaSource(path))
                    .filter(path -> !isArchitectureTestSupportSource(path))
                    .filter(this::containsDataObjectReference)
                    .forEach(violations::add);
        }

        assertTrue(
                "DO/DataObject must only be defined or referenced in sandwish-infra: " + violations,
                violations.isEmpty());
    }

    private boolean isProjectSource(Path path) {
        String value = path.toString();
        return value.endsWith(".java") || value.endsWith(".xml") || value.endsWith("pom.xml");
    }

    private boolean isMainJavaSource(Path path) {
        return path.toString().contains("/src/main/java/");
    }

    private boolean isInfraMainJavaSource(Path path) {
        return path.toString().contains("/sandwish-infra/src/main/java/");
    }

    private boolean isArchitectureTestSupportSource(Path path) {
        return path.toString().contains("/sandwish-common/sandwish-common-test/src/main/java/");
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

    private boolean containsEntityPreWriteHook(Path path) {
        try {
            String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            return content.contains(".preInsert()")
                    || content.contains(".preUpdate()")
                    || content.contains("::preInsert")
                    || content.contains("::preUpdate");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean containsDataObjectReference(Path path) {
        try {
            String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            return DATA_OBJECT_REFERENCE_PATTERN
                    .matcher(removeJavaComments(content))
                    .find();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private String removeJavaComments(String content) {
        StringBuilder builder = new StringBuilder(content.length());
        boolean inLineComment = false;
        boolean inBlockComment = false;
        for (int i = 0; i < content.length(); i++) {
            char current = content.charAt(i);
            char next = i + 1 < content.length() ? content.charAt(i + 1) : '\0';
            if (inLineComment) {
                if (current == '\n') {
                    inLineComment = false;
                    builder.append(current);
                }
                continue;
            }
            if (inBlockComment) {
                if (current == '*' && next == '/') {
                    inBlockComment = false;
                    i++;
                }
                continue;
            }
            if (current == '/' && next == '/') {
                inLineComment = true;
                i++;
                continue;
            }
            if (current == '/' && next == '*') {
                inBlockComment = true;
                i++;
                continue;
            }
            builder.append(current);
        }
        return builder.toString();
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
