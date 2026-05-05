package com.github.thundax.architecture;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.Test;

public class HasPermissionAnnotationArchitectureTest {

    private static final Pattern API_OPERATION_PATTERN =
            Pattern.compile("@ApiOperation\\([^)]*notes\\s*=\\s*\"([^\"]*)\"[^)]*\\)([\\s\\S]*?)public\\s+");

    @Test
    public void shouldDeclarePermissionWhenApiOperationNotesDeclarePermission() throws IOException {
        Path sourceRoot = projectRoot().resolve("sandwish-admin-api/src/main/java/com/github/thundax/modules");
        List<String> violations = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                    .forEach(path -> collectViolations(path, violations));
        }

        assertTrue(
                "API methods with non-empty/non-ignore notes must declare @HasPermission: " + violations,
                violations.isEmpty());
    }

    private static void collectViolations(Path path, List<String> violations) {
        String content;
        try {
            content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + path, e);
        }

        Matcher matcher = API_OPERATION_PATTERN.matcher(content);
        while (matcher.find()) {
            String notes = matcher.group(1).trim();
            String annotations = matcher.group(2);
            if (notes.length() > 0 && !"ignore".equals(notes) && !annotations.contains("@HasPermission")) {
                violations.add(projectRoot().relativize(path) + " notes=" + notes);
            }
        }
    }

    private static Path projectRoot() {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            if (Files.exists(current.resolve("pom.xml")) && Files.exists(current.resolve("sandwish-admin-api"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Cannot locate project root");
    }
}
