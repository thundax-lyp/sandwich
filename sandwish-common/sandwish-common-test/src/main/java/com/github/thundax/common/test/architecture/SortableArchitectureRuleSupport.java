package com.github.thundax.common.test.architecture;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class SortableArchitectureRuleSupport {

    private static final Pattern SORTABLE_CLASS_PATTERN =
            Pattern.compile("\\bpublic\\s+class\\s+([A-Za-z0-9_]+)\\s+implements\\s+([^\\{]+)");
    private static final Pattern FIELD_PATTERN = Pattern.compile(
            "\\bprivate\\s+(?:static\\s+final\\s+)?[A-Za-z0-9_<>, ?\\.\\[\\]]+\\s+([A-Za-z][A-Za-z0-9_]*)\\s*(?:=[^;]*)?;");
    private static final Pattern SORT_METHOD_PATTERN = Pattern.compile(
            "((?:\\s*@[^\\n]+\\n)+)\\s*public\\s+void\\s+sort\\s*\\(\\s*([A-Za-z0-9_]+SortCommand)\\s+command\\s*\\)");

    private SortableArchitectureRuleSupport() {}

    public static void assertSortableEntitiesAreAllowed(Path sourceRoot, Collection<String> allowedClassNames)
            throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith(".java"))
                    .forEach(path -> collectSortableClassViolations(root, path, allowedClassNames, violations));
        }

        assertTrue("Sortable entities must be explicitly allowed: " + violations, violations.isEmpty());
    }

    public static void assertSortCommandsAreAllowed(Path sourceRoot, Collection<String> allowedCommandNames)
            throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("SortCommand.java"))
                    .forEach(
                            path -> collectSortCommandAllowlistViolations(root, path, allowedCommandNames, violations));
        }

        assertTrue("Sort command classes must be explicitly allowed: " + violations, violations.isEmpty());
    }

    public static void assertSortCommandsUseOrderedIdsOnly(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("SortCommand.java"))
                    .forEach(path -> collectSortCommandFieldViolations(root, path, violations));
        }

        assertTrue("Sort commands must only expose orderedIds and sortDirection: " + violations, violations.isEmpty());
    }

    public static void assertSortMethodsAreTransactional(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("ServiceImpl.java"))
                    .forEach(path -> collectSortMethodTransactionViolations(root, path, violations));
        }

        assertTrue("Sort service methods must be transactional: " + violations, violations.isEmpty());
    }

    public static void assertFlatSortSchemasDeclarePriorityUnique(Path schemaRoot, Collection<String> tableNames)
            throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();
        String schemaContent = readSchemaContent(schemaRoot);
        for (String tableName : tableNames) {
            String tableBody = tableBody(schemaContent, tableName);
            if (tableBody.length() == 0 || !tableBody.contains("UNIQUE KEY") || !tableBody.contains("`priority`")) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, schemaRoot) + " table=" + tableName);
            }
        }

        assertTrue("FlatSort tables must declare unique priority keys: " + violations, violations.isEmpty());
    }

    private static void collectSortableClassViolations(
            Path root, Path path, Collection<String> allowedClassNames, List<String> violations) {
        String content = ArchitectureSourceSupport.readSourceWithoutComments(path);
        Matcher matcher = SORTABLE_CLASS_PATTERN.matcher(content);
        while (matcher.find()) {
            String className = matcher.group(1);
            String interfaces = matcher.group(2);
            if (interfaces.contains("Sortable") && !allowedClassNames.contains(className)) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " class=" + className);
            }
        }
    }

    private static void collectSortCommandAllowlistViolations(
            Path root, Path path, Collection<String> allowedCommandNames, List<String> violations) {
        String className = path.getFileName().toString().replace(".java", "");
        if (!allowedCommandNames.contains(className)) {
            violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " class=" + className);
        }
    }

    private static void collectSortCommandFieldViolations(Path root, Path path, List<String> violations) {
        String content = ArchitectureSourceSupport.readSourceWithoutComments(path);
        Matcher matcher = FIELD_PATTERN.matcher(content);
        while (matcher.find()) {
            String fieldName = matcher.group(1);
            if (!"orderedIds".equals(fieldName)
                    && !"sortDirection".equals(fieldName)
                    && !"serialVersionUID".equals(fieldName)) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " field=" + fieldName);
            }
        }
    }

    private static void collectSortMethodTransactionViolations(Path root, Path path, List<String> violations) {
        String content = ArchitectureSourceSupport.readSourceWithoutComments(path);
        Matcher matcher = SORT_METHOD_PATTERN.matcher(content);
        while (matcher.find()) {
            String annotations = matcher.group(1);
            String commandName = matcher.group(2);
            if (!annotations.contains("@Transactional")) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " command=" + commandName);
            }
        }
    }

    private static String readSchemaContent(Path schemaRoot) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (Stream<Path> paths = Files.walk(schemaRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith(".sql"))
                    .sorted()
                    .forEach(path -> builder.append(ArchitectureSourceSupport.readSource(path))
                            .append('\n'));
        }
        return builder.toString();
    }

    private static String tableBody(String schemaContent, String tableName) {
        Pattern pattern = Pattern.compile(
                "CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?`"
                        + Pattern.quote(tableName)
                        + "`\\s*\\((.*?)\\)\\s*ENGINE",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(schemaContent);
        return matcher.find() ? matcher.group(1) : "";
    }
}
