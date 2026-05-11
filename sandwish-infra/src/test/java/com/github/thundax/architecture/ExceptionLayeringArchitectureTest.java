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
    public void shouldNotDependOnBusinessOrApiExceptionTypes() throws IOException {
        List<String> violations = new ArrayList<String>();

        for (Path source : javaSources(Paths.get("sandwish-infra/src/main/java"))) {
            String content = read(source);
            if (content.contains("BizException")
                    || content.contains("DomainException")
                    || content.contains("SandwishException")
                    || content.contains("ApiException")
                    || content.contains("InfraException")
                    || content.contains("common.exception")
                    || content.contains("common.web.exception")) {
                violations.add(source.toString());
            }
        }

        assertTrue("Infra layer must expose technical exceptions only: " + violations, violations.isEmpty());
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
