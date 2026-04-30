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
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.Test;

public class DomainEntityArchitectureTest {

    private static final Pattern OLD_ENTITY_BASE_REFERENCE_PATTERN = Pattern.compile(
            "import\\s+com\\.github\\.thundax\\.common\\.persistence\\.(BaseEntity|DataEntity|AdminDataEntity|TreeEntity|AdminTreeEntity)"
                    + "|extends\\s+(BaseEntity|DataEntity|AdminDataEntity|TreeEntity|AdminTreeEntity)\\b");
    private static final Pattern JACKSON_REFERENCE_PATTERN =
            Pattern.compile("com\\.fasterxml\\.jackson\\.annotation\\.|@Json[A-Za-z0-9_]*\\b");

    private static final Set<String> LEGACY_OLD_ENTITY_BASE_SOURCES = new LinkedHashSet<>(Arrays.asList(
            "sandwish-biz/src/main/java/com/github/thundax/modules/assist/entity/base/BaseAsyncTask.java",
            "sandwish-biz/src/main/java/com/github/thundax/modules/assist/entity/base/BaseSignature.java",
            "sandwish-biz/src/main/java/com/github/thundax/modules/member/entity/base/BaseMember.java",
            "sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/base/BaseStorage.java",
            "sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/base/BaseStorageBusiness.java",
            "sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/base/BaseDict.java",
            "sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/base/BaseLog.java",
            "sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/base/BaseMenu.java",
            "sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/base/BaseOffice.java",
            "sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/base/BaseRole.java",
            "sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/base/BaseUploadFile.java",
            "sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/base/BaseUser.java",
            "sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/base/BaseUserEncrypt.java"));

    private static final Set<String> LEGACY_JACKSON_ENTITY_SOURCES = new LinkedHashSet<>(Arrays.asList(
            "sandwish-biz/src/main/java/com/github/thundax/modules/assist/entity/AsyncTask.java",
            "sandwish-biz/src/main/java/com/github/thundax/modules/assist/entity/Signature.java",
            "sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/AccessToken.java",
            "sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/LoginForm.java",
            "sandwish-biz/src/main/java/com/github/thundax/modules/member/entity/Member.java",
            "sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/Storage.java",
            "sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/StorageBusiness.java",
            "sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Dict.java",
            "sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Log.java",
            "sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Menu.java",
            "sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Office.java",
            "sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Role.java",
            "sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/User.java",
            "sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/UserEncrypt.java"));

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

    private boolean containsPattern(Path path, Pattern pattern) {
        try {
            String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            return pattern.matcher(removeJavaComments(content)).find();
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
