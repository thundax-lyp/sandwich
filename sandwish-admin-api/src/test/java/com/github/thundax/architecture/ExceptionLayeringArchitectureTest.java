package com.github.thundax.architecture;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.Test;

public class ExceptionLayeringArchitectureTest {

    @Test
    public void shouldKeepApiExceptionAdaptersInCommonExceptionPackage() throws IOException {
        List<String> violations = new ArrayList<String>();

        for (Path source : javaSources(Paths.get("sandwish-admin-api/src/main/java"))) {
            String content = read(source);
            String path = source.toString();
            if (content.contains("ApiException")
                    || content.contains("BannedAccountException")
                    || content.contains("InvalidUsernamePasswordException")
                    || (content.contains("ExceptionTranslator") && !path.contains("/common/exception/"))
                    || (content.contains("extends SandwishException") && !path.contains("/common/exception/"))) {
                violations.add(path);
            }
        }

        assertTrue(
                "Admin API exception adapters must live in common.exception and ApiException is forbidden: "
                        + violations,
                violations.isEmpty());
    }

    private List<Path> javaSources(Path root) throws IOException {
        List<Path> sources = new ArrayList<Path>();
        try (Stream<Path> stream = Files.walk(root)) {
            stream.filter(path -> path.toString().endsWith(".java")).forEach(sources::add);
        }
        return sources;
    }

    private String read(Path source) throws IOException {
        return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
    }
}
