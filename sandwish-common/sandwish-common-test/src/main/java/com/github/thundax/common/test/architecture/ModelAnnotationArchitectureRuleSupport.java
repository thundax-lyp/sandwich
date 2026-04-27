package com.github.thundax.common.test.architecture;

import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;

public final class ModelAnnotationArchitectureRuleSupport {

    public static final String NAME_REQUEST_REQUIRED_ANNOTATIONS = "MODEL_REQUEST_REQUIRED_ANNOTATIONS";

    private static final Set<String> REQUEST_REQUIRED_ANNOTATIONS = new LinkedHashSet<String>(Arrays.asList(
            "lombok.Getter",
            "lombok.Setter",
            "io.swagger.annotations.ApiModel",
            "com.fasterxml.jackson.annotation.JsonInclude",
            "com.fasterxml.jackson.annotation.JsonIgnoreProperties"));

    private static final String SOURCE_ROOTS_PROPERTY = "sandwish.architecture.sourceRoots";

    private ModelAnnotationArchitectureRuleSupport() {
    }

    public static ArchRule requestClassAnnotationsRequired(String basePackage) {
        return ArchRuleDefinition.classes()
                .should(new ArchCondition<JavaClass>("declare required Request class annotations only") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        if (!isRequestClassUnder(item, basePackage)) {
                            return;
                        }
                        Set<String> actualAnnotations = annotationTypeNames(item);
                        Set<String> missingAnnotations = missingAnnotations(actualAnnotations, REQUEST_REQUIRED_ANNOTATIONS);
                        Set<String> extraAnnotations = missingAnnotations(REQUEST_REQUIRED_ANNOTATIONS, actualAnnotations);

                        if (!missingAnnotations.isEmpty() || !extraAnnotations.isEmpty()) {
                            events.add(SimpleConditionEvent.violated(
                                    item,
                                    "[" + NAME_REQUEST_REQUIRED_ANNOTATIONS + "] " + item.getFullName()
                                            + " violates Request class annotations: missing="
                                            + missingAnnotations + ", extra=" + extraAnnotations));
                        }
                    }
                })
                .allowEmptyShould(true)
                .because("RULE " + NAME_REQUEST_REQUIRED_ANNOTATIONS);
    }

    private static boolean isRequestClassUnder(JavaClass item, String basePackage) {
        return item.getPackageName().startsWith(basePackage + ".")
                && item.getPackageName().contains(".request")
                && item.getSimpleName().endsWith("Request");
    }

    private static Set<String> annotationTypeNames(JavaClass item) {
        Set<String> annotationTypeNames = new LinkedHashSet<String>();
        for (JavaAnnotation<JavaClass> annotation : item.getAnnotations()) {
            annotationTypeNames.add(annotation.getRawType().getFullName());
        }
        annotationTypeNames.addAll(sourceAnnotationTypeNames(item));
        return annotationTypeNames;
    }

    private static Set<String> missingAnnotations(Set<String> actualAnnotations, Set<String> expectedAnnotations) {
        Set<String> missingAnnotations = new LinkedHashSet<String>(expectedAnnotations);
        missingAnnotations.removeAll(actualAnnotations);
        return missingAnnotations;
    }

    private static Set<String> sourceAnnotationTypeNames(JavaClass item) {
        Path sourcePath = sourcePath(item);
        if (sourcePath == null) {
            return new LinkedHashSet<String>();
        }
        try {
            List<String> lines = Files.readAllLines(sourcePath, StandardCharsets.UTF_8);
            return sourceAnnotationTypeNames(item, lines);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read source file: " + sourcePath, ex);
        }
    }

    private static Path sourcePath(JavaClass item) {
        String sourcePath = item.getFullName().replace('.', '/') + ".java";
        for (String sourceRoot : sourceRoots()) {
            Path path = Paths.get(sourceRoot, sourcePath);
            if (Files.exists(path)) {
                return path;
            }
        }
        return null;
    }

    private static List<String> sourceRoots() {
        String configuredSourceRoots = System.getProperty(SOURCE_ROOTS_PROPERTY, "src/main/java,src/test/java");
        return Arrays.asList(configuredSourceRoots.split(","));
    }

    private static Set<String> sourceAnnotationTypeNames(JavaClass item, List<String> lines) {
        Map<String, String> imports = imports(lines);
        Set<String> annotationTypeNames = new LinkedHashSet<String>();
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.contains(" class " + item.getSimpleName())
                    || trimmedLine.contains(" interface " + item.getSimpleName())
                    || trimmedLine.contains(" enum " + item.getSimpleName())) {
                break;
            }
            if (trimmedLine.startsWith("@")) {
                annotationTypeNames.add(resolveAnnotationName(item, imports, annotationSimpleName(trimmedLine)));
            }
        }
        return annotationTypeNames;
    }

    private static Map<String, String> imports(List<String> lines) {
        Map<String, String> imports = new HashMap<String, String>();
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (!trimmedLine.startsWith("import ") || trimmedLine.startsWith("import static ")) {
                continue;
            }
            String importedClass = trimmedLine.substring("import ".length(), trimmedLine.length() - 1);
            imports.put(importedClass.substring(importedClass.lastIndexOf('.') + 1), importedClass);
        }
        return imports;
    }

    private static String annotationSimpleName(String line) {
        int endIndex = 1;
        while (endIndex < line.length()) {
            char ch = line.charAt(endIndex);
            if (!Character.isJavaIdentifierPart(ch) && ch != '.') {
                break;
            }
            endIndex++;
        }
        return line.substring(1, endIndex);
    }

    private static String resolveAnnotationName(JavaClass item, Map<String, String> imports, String annotationName) {
        if (annotationName.contains(".")) {
            return annotationName;
        }
        if (imports.containsKey(annotationName)) {
            return imports.get(annotationName);
        }
        return item.getPackageName() + "." + annotationName;
    }
}
