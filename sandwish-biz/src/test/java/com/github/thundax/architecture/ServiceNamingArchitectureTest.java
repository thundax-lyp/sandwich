package com.github.thundax.architecture;

import static org.junit.Assert.assertTrue;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
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

public class ServiceNamingArchitectureTest extends AbstractArchitectureTest {

    private static final Set<String> LEGACY_SERVICE_METHOD_NAMES =
            new LinkedHashSet<>(Arrays.asList("get", "getMany", "find", "findList", "findPage", "findOne", "delete"));
    private static final Pattern SERVICE_QUERY_SETTER_DECLARATION_PATTERN =
            Pattern.compile("\\bpublic\\s+void\\s+set[A-Z][A-Za-z0-9_]*\\s*\\(");

    @Test
    public void shouldUseServiceMethodShape() {
        JavaClasses classes = importPackages("com.github.thundax.common.service", "com.github.thundax.modules");
        List<String> violations = new ArrayList<>();

        for (JavaClass javaClass : classes) {
            if (!isServiceInterface(javaClass)) {
                continue;
            }
            for (JavaMethod method : javaClass.getMethods()) {
                if (isLegacyServiceMethod(method)) {
                    violations.add(method.getFullName());
                }
            }
        }

        assertTrue(
                "Service methods must use getById/getByXxx/list/page/count/deleteById/batchXxx for generic access "
                        + "and business verbs for workflows: "
                        + violations,
                violations.isEmpty());
    }

    @Test
    public void shouldPlaceServiceQueryObjectsUnderServiceQueryPackage() {
        JavaClasses classes = importPackages("com.github.thundax.modules");
        List<String> violations = new ArrayList<>();

        for (JavaClass javaClass : classes) {
            if (isServiceQueryObject(javaClass) && !isInServiceQueryPackage(javaClass)) {
                violations.add(javaClass.getName());
            }
        }

        assertTrue(
                "Service query objects must be placed under "
                        + "com.github.thundax.modules.{module}.service.query: "
                        + violations,
                violations.isEmpty());
    }

    @Test
    public void shouldNotDeclareSettersInServiceQueryObjects() throws IOException {
        Path root = repositoryRoot();
        Path sourceRoot = root.resolve("sandwish-biz").resolve("src/main/java/com/github/thundax/modules");
        List<String> violations = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(Files::isRegularFile)
                    .filter(this::isServiceQuerySource)
                    .filter(path -> containsPattern(path, SERVICE_QUERY_SETTER_DECLARATION_PATTERN))
                    .map(path -> toRepositoryPath(root, path))
                    .forEach(violations::add);
        }

        assertTrue(
                "Service query objects must only define query fields; request-to-query conversion belongs in "
                        + "InterfaceAssembler, so service query source must not declare setXxx methods: "
                        + violations,
                violations.isEmpty());
    }

    private boolean isServiceInterface(JavaClass javaClass) {
        return javaClass.isInterface()
                && javaClass.getSimpleName().endsWith("Service")
                && javaClass.getPackageName().contains(".service");
    }

    private boolean isLegacyServiceMethod(JavaMethod method) {
        return LEGACY_SERVICE_METHOD_NAMES.contains(method.getName())
                || method.getName().startsWith("find");
    }

    private boolean isServiceQueryObject(JavaClass javaClass) {
        String simpleName = javaClass.getSimpleName();
        return simpleName.endsWith("Query") && !"Query".equals(simpleName);
    }

    private boolean isInServiceQueryPackage(JavaClass javaClass) {
        return javaClass.getPackageName().contains(".service.query");
    }

    private boolean isServiceQuerySource(Path path) {
        String value = normalizePath(path);
        return value.contains("/modules/") && value.contains("/service/query/") && value.endsWith("Query.java");
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
