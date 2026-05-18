package com.github.thundax.common.test.architecture;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class JsonBoundaryArchitectureTest {

    private static final List<ForbiddenDependency> FORBIDDEN_JSON_DEPENDENCIES = Arrays.asList(
            new ForbiddenDependency("com.alibaba", "fastjson"),
            new ForbiddenDependency("com.alibaba.fastjson2", null),
            new ForbiddenDependency("net.sf.json-lib", "json-lib"),
            new ForbiddenDependency("com.google.code.gson", "gson"),
            new ForbiddenDependency("org.json", "json"),
            new ForbiddenDependency("com.eclipsesource.minimal-json", "minimal-json"),
            new ForbiddenDependency("com.squareup.moshi", null),
            new ForbiddenDependency("javax.json", null),
            new ForbiddenDependency("jakarta.json", null));

    private static final Pattern FORBIDDEN_JSON_SOURCE_PATTERN =
            Pattern.compile("\\b(com\\.alibaba\\.fastjson|com\\.alibaba\\.fastjson2|net\\.sf\\.json|com\\.google\\.gson"
                    + "|org\\.json|javax\\.json|jakarta\\.json|com\\.eclipsesource\\.json|com\\.squareup\\.moshi)\\b");

    @Test
    public void shouldNotDeclareNonJacksonJsonDependenciesInPom() throws Exception {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> "pom.xml".equals(path.getFileName().toString()))
                    .forEach(path -> collectPomViolations(root, path, violations));
        }

        assertTrue(
                "Project JSON boundary must use Spring Boot default Jackson only. Forbidden JSON dependencies: "
                        + violations,
                violations.isEmpty());
    }

    @Test
    public void shouldNotUseNonJacksonJsonPackagesInMainSource() throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                    .filter(path ->
                            ArchitectureSourceSupport.normalizePath(path).contains("/src/main/java/"))
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(this::containsForbiddenJsonPackage)
                    .map(path -> ArchitectureSourceSupport.repositoryPath(root, path))
                    .forEach(violations::add);
        }

        assertTrue(
                "Project JSON boundary must use Spring Boot default Jackson only. Forbidden JSON package usage: "
                        + violations,
                violations.isEmpty());
    }

    private void collectPomViolations(Path root, Path pom, List<String> violations) {
        Document document = parseXml(pom);
        NodeList dependencies = document.getElementsByTagName("dependency");
        for (int i = 0; i < dependencies.getLength(); i++) {
            Element dependency = (Element) dependencies.item(i);
            String groupId = childText(dependency, "groupId");
            String artifactId = childText(dependency, "artifactId");
            if (isForbiddenJsonDependency(groupId, artifactId)) {
                violations.add(
                        ArchitectureSourceSupport.repositoryPath(root, pom) + " -> " + groupId + ":" + artifactId);
            }
        }
    }

    private boolean isForbiddenJsonDependency(String groupId, String artifactId) {
        for (ForbiddenDependency dependency : FORBIDDEN_JSON_DEPENDENCIES) {
            if (dependency.matches(groupId, artifactId)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsForbiddenJsonPackage(Path path) {
        return FORBIDDEN_JSON_SOURCE_PATTERN
                .matcher(ArchitectureSourceSupport.readSourceWithoutComments(path))
                .find();
    }

    private Document parseXml(Path path) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            return factory.newDocumentBuilder().parse(path.toFile());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new IllegalStateException("Failed to parse pom: " + path, e);
        }
    }

    private String childText(Element parent, String name) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element && name.equals(child.getNodeName())) {
                return child.getTextContent().trim();
            }
        }
        return "";
    }

    private static final class ForbiddenDependency {

        private final String groupId;
        private final String artifactId;

        private ForbiddenDependency(String groupId, String artifactId) {
            this.groupId = groupId;
            this.artifactId = artifactId;
        }

        private boolean matches(String candidateGroupId, String candidateArtifactId) {
            boolean groupMatches = groupId.equals(candidateGroupId);
            boolean artifactMatches = artifactId == null || artifactId.equals(candidateArtifactId);
            return groupMatches && artifactMatches;
        }
    }
}
