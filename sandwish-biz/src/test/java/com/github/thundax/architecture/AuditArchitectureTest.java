package com.github.thundax.architecture;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.Test;

public class AuditArchitectureTest {

    private static final Pattern BUSINESS_AUDIT_FIELD_PATTERN =
            Pattern.compile("\\b(createUserId|updateUserId|createBy|updateBy)\\b");

    @Test
    public void shouldNotReintroduceBusinessAuditFieldsInBizModules() throws IOException {
        Path root = repositoryRoot();
        Path sourceRoot = root.resolve("sandwish-biz").resolve("src/main/java/com/github/thundax/modules");
        List<String> violations = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> !normalizePath(path).contains("/modules/audit/"))
                    .filter(path -> BUSINESS_AUDIT_FIELD_PATTERN.matcher(read(path)).find())
                    .map(path -> normalizePath(root.relativize(path)))
                    .forEach(violations::add);
        }

        assertTrue("Business modules must not reintroduce generic audit fields: " + violations, violations.isEmpty());
    }

    private String read(Path path) {
        try {
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private Path repositoryRoot() {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            if (Files.exists(current.resolve("TODO.md"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Can not find repository root");
    }

    private String normalizePath(Path path) {
        return path.toString().replace('\\', '/');
    }
}
