package com.github.thundax.architecture;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.Test;

public class DomainEntityArchitectureTest {

    private static final Pattern OLD_ENTITY_BASE_REFERENCE_PATTERN = Pattern.compile(
            "import\\s+com\\.github\\.thundax\\.common\\.persistence\\.(BaseEntity|DataEntity|AdminDataEntity|TreeEntity|AdminTreeEntity)"
                    + "|extends\\s+(BaseEntity|DataEntity|AdminDataEntity|TreeEntity|AdminTreeEntity)\\b");
    private static final Pattern JACKSON_REFERENCE_PATTERN =
            Pattern.compile("com\\.fasterxml\\.jackson\\.annotation\\.|@Json[A-Za-z0-9_]*\\b");
    private static final Pattern ENUM_ILLEGAL_ARGUMENT_EXCEPTION_PATTERN =
            Pattern.compile("IllegalArgumentException\\s*\\(");
    private static final Pattern ENUM_DECLARATION_PATTERN = Pattern.compile("\\benum\\s+[A-Za-z0-9_]+\\b");
    private static final Pattern ENUM_FROM_STRING_PATTERN =
            Pattern.compile("\\bfrom\\s*\\(\\s*String\\s+[A-Za-z0-9_]+\\s*\\)");
    private static final Pattern BIZ_EXCEPTION_PATTERN = Pattern.compile("\\bBizException\\b");

    private static final Set<String> LEGACY_OLD_ENTITY_BASE_SOURCES = new LinkedHashSet<>();

    private static final Set<String> LEGACY_JACKSON_ENTITY_SOURCES = new LinkedHashSet<>();

    @Test
    public void shouldNotIntroduceOldEntityBaseDependencyInDomainEntities() throws IOException {
        List<String> violations = findViolations(OLD_ENTITY_BASE_REFERENCE_PATTERN, LEGACY_OLD_ENTITY_BASE_SOURCES);

        assertTrue(
                "Domain entities must not introduce old persistence entity base dependencies: " + violations,
                violations.isEmpty());
    }

    @Test
    public void shouldNotIntroduceJacksonDependencyInDomainEntities() throws IOException {
        List<String> violations = findViolations(JACKSON_REFERENCE_PATTERN, LEGACY_JACKSON_ENTITY_SOURCES);

        assertTrue(
                "Domain entities must not introduce controller JSON serialization dependencies: " + violations,
                violations.isEmpty());
    }

    @Test
    public void shouldUseBizExceptionForDomainEnumParsingFailures() throws IOException {
        Path root = repositoryRoot();
        Path sourceRoot = root.resolve("sandwish-biz").resolve("src/main/java/com/github/thundax/modules");
        List<String> violations = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(this::isModuleEnumSource)
                    .filter(path -> containsPattern(path, ENUM_DECLARATION_PATTERN))
                    .forEach(path -> collectEnumParsingViolation(root, path, violations));
        }

        assertTrue("Domain enum parsing failures must use BizException: " + violations, violations.isEmpty());
    }

    private List<String> findViolations(Pattern pattern, Set<String> legacySources) throws IOException {
        Path root = repositoryRoot();
        Path sourceRoot = root.resolve("sandwish-biz").resolve("src/main/java/com/github/thundax/modules");
        List<String> violations = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(this::isModuleEntitySource)
                    .filter(path -> containsPattern(path, pattern))
                    .map(path -> toRepositoryPath(root, path))
                    .filter(path -> !legacySources.contains(path))
                    .forEach(violations::add);
        }
        return violations;
    }

    private boolean isModuleEntitySource(Path path) {
        String value = normalizePath(path);
        return value.contains("/modules/") && value.contains("/entity/");
    }

    private boolean isModuleEnumSource(Path path) {
        String value = normalizePath(path);
        return value.contains("/modules/") && value.contains("/entity/") && value.endsWith(".java");
    }

    private void collectEnumParsingViolation(Path root, Path path, List<String> violations) {
        String content = readSourceWithoutComments(path);
        boolean hasFromString = ENUM_FROM_STRING_PATTERN.matcher(content).find();
        boolean hasIllegalArgumentException =
                ENUM_ILLEGAL_ARGUMENT_EXCEPTION_PATTERN.matcher(content).find();
        boolean missesBizException =
                hasFromString && !BIZ_EXCEPTION_PATTERN.matcher(content).find();
        if (hasIllegalArgumentException || missesBizException) {
            violations.add(toRepositoryPath(root, path));
        }
    }

    private boolean containsPattern(Path path, Pattern pattern) {
        return pattern.matcher(readSourceWithoutComments(path)).find();
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
