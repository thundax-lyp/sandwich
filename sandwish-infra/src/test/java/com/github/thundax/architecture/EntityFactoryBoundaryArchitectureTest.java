package com.github.thundax.architecture;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.Test;

public class EntityFactoryBoundaryArchitectureTest {

    private static final List<String> BUSINESS_MODULES =
            Arrays.asList("sandwish-biz", "sandwish-admin-api", "sandwish-front-api");
    private static final Pattern ENTITY_IMPORT_PATTERN =
            Pattern.compile("import\\s+com\\.github\\.thundax\\.modules\\.[^;]+\\.entity\\.([A-Z][A-Za-z0-9]+)\\s*;");

    @Test
    public void infraShouldNotCreateDomainEntities() throws IOException {
        Path root = repositoryRoot();
        Path sourceRoot = root.resolve("sandwish-infra").resolve("src/main/java");
        List<String> violations = findEntityFactoryCallViolations(root, sourceRoot, "create");

        assertTrue("Infra must not call entity create methods: " + violations, violations.isEmpty());
    }

    @Test
    public void businessSourcesShouldNotRestoreDomainEntities() throws IOException {
        Path root = repositoryRoot();
        List<String> violations = new ArrayList<>();
        for (String module : BUSINESS_MODULES) {
            Path sourceRoot = root.resolve(module).resolve("src/main/java");
            if (Files.exists(sourceRoot)) {
                violations.addAll(findEntityFactoryCallViolations(root, sourceRoot, "restore"));
            }
        }

        assertTrue("Business sources must not call entity restore methods: " + violations, violations.isEmpty());
    }

    private List<String> findEntityFactoryCallViolations(Path root, Path sourceRoot, String methodName)
            throws IOException {
        List<String> violations = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> !isModuleEntitySource(path))
                    .forEach(path -> collectEntityFactoryCallViolations(root, path, methodName, violations));
        }
        return violations;
    }

    private void collectEntityFactoryCallViolations(Path root, Path path, String methodName, List<String> violations) {
        String source = readSourceWithoutComments(path);
        for (String entityName : importedEntityNames(source)) {
            if (Pattern.compile("\\b" + entityName + "\\s*\\.\\s*" + methodName + "\\s*\\(")
                    .matcher(source)
                    .find()) {
                violations.add(toRepositoryPath(root, path) + "#" + entityName + "." + methodName);
            }
        }
    }

    private Set<String> importedEntityNames(String source) {
        Set<String> names = new LinkedHashSet<>();
        Matcher matcher = ENTITY_IMPORT_PATTERN.matcher(source);
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names;
    }

    private boolean isModuleEntitySource(Path path) {
        String value = normalizePath(path);
        return value.contains("/modules/") && value.contains("/entity/");
    }

    private String readSourceWithoutComments(Path path) {
        try {
            String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            return removeJavaComments(content);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private String toRepositoryPath(Path root, Path path) {
        return normalizePath(root.relativize(path));
    }

    private String normalizePath(Path path) {
        return path.toString().replace('\\', '/');
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
