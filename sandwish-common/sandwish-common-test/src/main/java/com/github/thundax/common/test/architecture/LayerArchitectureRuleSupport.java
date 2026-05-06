package com.github.thundax.common.test.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.Assert.assertTrue;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.ArchRule;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class LayerArchitectureRuleSupport {

    private static final Set<String> DEFAULT_LEGACY_SERVICE_BOUNDARY_DIRTY_TYPES = Collections.emptySet();
    private static final Pattern PACKAGE_DECLARATION_PATTERN = Pattern.compile("\\bpackage\\s+([a-zA-Z0-9_.]+)\\s*;");
    private static final Pattern CONTRACT_METHOD_DECLARATION_PATTERN =
            Pattern.compile("(?m)^(\\s*(?:@[\\w.]+(?:\\([^\\n]*\\))?\\s*)*)(?:default\\s+)?"
                    + "(?:<[\\w, ? extends super]+>\\s+)?[\\w.$<>\\[\\], ? extends super]+\\s+"
                    + "(\\w+)\\s*\\([^;{}]*\\)\\s*(?:;|\\{)");
    private static final Pattern LAYER_PUBLIC_API_ANNOTATION_PATTERN =
            Pattern.compile("@(?:[\\w.]+\\.)?LayerPublicApi\\s*\\(([^)]*)\\)");
    private static final Pattern LAYER_PUBLIC_API_REASON_PATTERN = Pattern.compile("\\breason\\s*=\\s*\"([^\"]*)\"");

    private LayerArchitectureRuleSupport() {}

    public static ArchRule interfaceAssemblersShouldNotBeSpringComponents() {
        return noClasses()
                .that()
                .haveSimpleNameEndingWith("InterfaceAssembler")
                .should()
                .beAnnotatedWith("org.springframework.stereotype.Component");
    }

    public static ArchRule interfaceAssemblersShouldNotDependOnServices() {
        return noClasses()
                .that()
                .haveSimpleNameEndingWith("InterfaceAssembler")
                .should()
                .dependOnClassesThat()
                .haveSimpleNameEndingWith("Service");
    }

    public static ArchRule restControllersShouldNotDependOnValidator() {
        return noClasses()
                .that()
                .resideInAPackage("..controller..")
                .and()
                .areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .should()
                .dependOnClassesThat()
                .areAssignableTo("javax.validation.Validator");
    }

    public static ArchRule serviceApiInterfacesShouldNotExist() {
        return noClasses()
                .that()
                .resideInAPackage("..api..")
                .should()
                .haveSimpleNameEndingWith("ServiceApi")
                .allowEmptyShould(true);
    }

    public static ArchRule servletRegistrationBeanShouldNotBeUsed() {
        return noClasses()
                .should()
                .dependOnClassesThat()
                .areAssignableTo("org.springframework.boot.web.servlet.ServletRegistrationBean");
    }

    public static ArchRule businessModulesShouldNotDeclareServletEndpoints() {
        return noClasses()
                .that()
                .resideInAPackage("..modules..")
                .should()
                .beAssignableTo("javax.servlet.http.HttpServlet");
    }

    public static ArchRule businessModulesShouldNotUseServletPackages() {
        return noClasses().should().resideInAPackage("..servlet..");
    }

    public static void assertServiceBoundaryTypesClean(JavaClasses classes) {
        assertServiceBoundaryTypesClean(classes, DEFAULT_LEGACY_SERVICE_BOUNDARY_DIRTY_TYPES);
    }

    public static void assertServiceBoundaryTypesClean(JavaClasses classes, Set<String> legacyDirtyTypes) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (!isServiceInterface(javaClass)) {
                continue;
            }
            for (JavaMethod method : javaClass.getMethods()) {
                collectServiceReturnTypeViolation(method, method.getRawReturnType(), violations, legacyDirtyTypes);
                for (JavaClass parameterType : method.getRawParameterTypes()) {
                    collectServiceParameterTypeViolation(method, parameterType, violations, legacyDirtyTypes);
                }
            }
        }

        assertTrue(
                "Service method parameters must be *DTO, *Query, Entity, or Java-Type, and return values must be "
                        + "*DTO, Entity, or Java-Type. Known dirty types that still need cleanup are "
                        + legacyDirtyTypes
                        + ". New violations: "
                        + violations,
                violations.isEmpty());
    }

    public static void assertDaoBoundaryTypesClean(JavaClasses classes) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (!isDaoInterface(javaClass)) {
                continue;
            }
            for (JavaMethod method : javaClass.getMethods()) {
                collectDaoReturnViolation(method, method.getRawReturnType(), violations);
                for (JavaClass parameterType : method.getRawParameterTypes()) {
                    collectDaoParameterViolation(method, parameterType, violations);
                }
            }
        }

        assertTrue(
                "DAO method boundary types must be Entity, Java standard types, or MyBatis-Plus Page<Entity> "
                        + "for page return values. Violations: "
                        + violations,
                violations.isEmpty());
    }

    public static void assertNoEmptyServiceBaseTypes(JavaClasses classes) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (isEmptyServiceBaseType(javaClass)) {
                violations.add(javaClass.getName());
            }
        }

        assertTrue(
                "Empty BaseService/BaseServiceImpl style types have no Service boundary value: " + violations,
                violations.isEmpty());
    }

    public static void assertLayerPublicApiMethodsAreNotTestOnly() {
        assertLayerPublicApiMethodsAreNotTestOnly(Collections.emptySet());
    }

    public static void assertLayerPublicApiMethodsAreNotTestOnly(Set<String> allowedMethods) {
        List<SourceFile> mainSources = sources("/src/main/java/");
        List<SourceFile> testSources = sources("/src/test/java/");
        List<LayerMethod> methods = new ArrayList<LayerMethod>();

        for (SourceFile source : mainSources) {
            if (isLayerPublicApiSource(source.path)) {
                methods.addAll(layerMethods(source));
            }
        }

        List<String> violations = new ArrayList<String>();
        for (LayerMethod method : methods) {
            if (method.hasLayerPublicApi && !isAcceptedLayerPublicApiReason(method.reason)) {
                violations.add(method.fullName + " declares invalid @LayerPublicApi reason in " + method.path);
                continue;
            }
            if (allowedMethods.contains(method.fullName)
                    || hasProductionCaller(method, mainSources)
                    || !hasTestCaller(method, testSources)
                    || method.hasLayerPublicApi) {
                continue;
            }
            violations.add(method.fullName + " declared in " + method.path);
        }

        assertTrue(
                "Service, DAO and Mapper public API methods must not be used only by tests unless they declare "
                        + "@LayerPublicApi with a concrete non-test reason. Violations: "
                        + violations,
                violations.isEmpty());
    }

    private static void collectServiceReturnTypeViolation(
            JavaMethod method, JavaClass type, List<String> violations, Set<String> legacyDirtyTypes) {
        if (isAllowedServiceReturnType(type) || legacyDirtyTypes.contains(type.getName())) {
            return;
        }
        violations.add(method.getFullName() + " has invalid return type " + type.getName());
    }

    private static void collectServiceParameterTypeViolation(
            JavaMethod method, JavaClass type, List<String> violations, Set<String> legacyDirtyTypes) {
        if (isAllowedServiceParameterType(type) || legacyDirtyTypes.contains(type.getName())) {
            return;
        }
        violations.add(method.getFullName() + " has invalid parameter type " + type.getName());
    }

    private static void collectDaoReturnViolation(JavaMethod method, JavaClass type, List<String> violations) {
        if (isAllowedDaoResultType(method, type)) {
            return;
        }
        violations.add(method.getFullName() + " has invalid return type " + type.getName());
    }

    private static void collectDaoParameterViolation(JavaMethod method, JavaClass type, List<String> violations) {
        if (isAllowedDaoParameterType(type)) {
            return;
        }
        violations.add(method.getFullName() + " has invalid parameter type " + type.getName());
    }

    private static boolean isAllowedServiceReturnType(JavaClass type) {
        return isVoid(type) || isJavaType(type) || isModuleEntity(type) || isDto(type);
    }

    private static boolean isAllowedServiceParameterType(JavaClass type) {
        return isAllowedServiceReturnType(type) || isServiceQuery(type);
    }

    private static boolean isAllowedDaoResultType(JavaMethod method, JavaClass type) {
        return isAllowedDaoParameterType(type) || isMyBatisPlusPage(type) && "page".equals(method.getName());
    }

    private static boolean isAllowedDaoParameterType(JavaClass type) {
        return isJavaType(type) || isModuleEntity(type);
    }

    private static boolean isServiceInterface(JavaClass javaClass) {
        return javaClass.isInterface()
                && javaClass.getSimpleName().endsWith("Service")
                && javaClass.getPackageName().contains(".service");
    }

    private static boolean isDaoInterface(JavaClass javaClass) {
        return javaClass.isInterface()
                && javaClass.getSimpleName().endsWith("Dao")
                && javaClass.getPackageName().contains(".dao");
    }

    private static boolean isVoid(JavaClass type) {
        return "void".equals(type.getName()) || "java.lang.Void".equals(type.getName());
    }

    private static boolean isJavaType(JavaClass type) {
        if (type.isPrimitive() || type.isEnum()) {
            return true;
        }
        if (type.isArray()) {
            return isJavaType(type.getBaseComponentType())
                    || isModuleEntity(type.getBaseComponentType())
                    || isDto(type.getBaseComponentType())
                    || isServiceQuery(type.getBaseComponentType());
        }
        return type.getName().startsWith("java.") || type.getName().startsWith("com.github.thundax.common.id.");
    }

    private static boolean isModuleEntity(JavaClass type) {
        return type.getPackageName().contains(".modules.")
                && type.getPackageName().contains(".entity");
    }

    private static boolean isDto(JavaClass type) {
        return type.getSimpleName().endsWith("DTO");
    }

    private static boolean isServiceQuery(JavaClass type) {
        return type.getSimpleName().endsWith("Query") && type.getPackageName().contains(".service.query");
    }

    private static boolean isMyBatisPlusPage(JavaClass type) {
        return "com.baomidou.mybatisplus.extension.plugins.pagination.Page".equals(type.getName());
    }

    private static boolean isEmptyServiceBaseType(JavaClass javaClass) {
        return javaClass.getSimpleName().equals("BaseService")
                || javaClass.getSimpleName().equals("BaseServiceImpl")
                || javaClass.getSimpleName().endsWith("BaseService")
                || javaClass.getSimpleName().endsWith("BaseServiceImpl");
    }

    private static List<SourceFile> sources(String sourceRoot) {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        try (Stream<Path> paths = Files.walk(root)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> ArchitectureSourceSupport.normalizePath(root.relativize(path))
                            .contains(sourceRoot))
                    .map(path -> new SourceFile(
                            ArchitectureSourceSupport.repositoryPath(root, path),
                            ArchitectureSourceSupport.readSource(path),
                            ArchitectureSourceSupport.readSourceWithoutComments(path)))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean isLayerPublicApiSource(String path) {
        return path.contains("/modules/")
                && (isServiceContractSource(path) || isDaoContractSource(path) || isMapperSource(path));
    }

    private static boolean isServiceContractSource(String path) {
        return path.contains("/service/") && path.endsWith("Service.java") && !path.contains("/service/impl/");
    }

    private static boolean isDaoContractSource(String path) {
        return path.contains("/dao/") && path.endsWith("Dao.java") && !path.contains("/persistence/dao/");
    }

    private static boolean isMapperSource(String path) {
        return path.contains("/persistence/mapper/") && path.endsWith("Mapper.java");
    }

    private static List<LayerMethod> layerMethods(SourceFile source) {
        String packageName = packageName(source.code);
        String simpleName = simpleName(source.path);
        Set<String> seen = new HashSet<String>();
        List<LayerMethod> methods = new ArrayList<LayerMethod>();
        Matcher matcher = CONTRACT_METHOD_DECLARATION_PATTERN.matcher(source.code);
        while (matcher.find()) {
            String annotations = matcher.group(1);
            String methodName = matcher.group(2);
            if (simpleName.equals(methodName) || !seen.add(methodName)) {
                continue;
            }
            methods.add(new LayerMethod(
                    packageName + "." + simpleName + "#" + methodName,
                    methodName,
                    source.path,
                    hasLayerPublicApi(annotations),
                    layerPublicApiReason(annotations)));
        }
        return methods;
    }

    private static boolean hasLayerPublicApi(String annotations) {
        return LAYER_PUBLIC_API_ANNOTATION_PATTERN.matcher(annotations).find();
    }

    private static String layerPublicApiReason(String annotations) {
        Matcher annotation = LAYER_PUBLIC_API_ANNOTATION_PATTERN.matcher(annotations);
        if (!annotation.find()) {
            return null;
        }
        Matcher reason = LAYER_PUBLIC_API_REASON_PATTERN.matcher(annotation.group(1));
        return reason.find() ? reason.group(1) : null;
    }

    private static boolean isAcceptedLayerPublicApiReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            return false;
        }
        String normalizedReason = reason.toLowerCase();
        return !normalizedReason.contains("test")
                && !normalizedReason.contains("case")
                && !normalizedReason.contains("测试");
    }

    private static String packageName(String content) {
        Matcher matcher = PACKAGE_DECLARATION_PATTERN.matcher(content);
        if (!matcher.find()) {
            throw new IllegalStateException("Can not find package declaration");
        }
        return matcher.group(1);
    }

    private static String simpleName(String path) {
        int start = path.lastIndexOf('/') + 1;
        int end = path.length() - ".java".length();
        return path.substring(start, end);
    }

    private static boolean hasProductionCaller(LayerMethod method, List<SourceFile> mainSources) {
        return hasCaller(method, mainSources);
    }

    private static boolean hasTestCaller(LayerMethod method, List<SourceFile> testSources) {
        return hasCaller(method, testSources);
    }

    private static boolean hasCaller(LayerMethod method, List<SourceFile> sources) {
        Pattern callPattern = Pattern.compile("(?:\\.|::)\\s*" + Pattern.quote(method.name) + "\\s*\\(");
        for (SourceFile source : sources) {
            if (method.path.equals(source.path)) {
                continue;
            }
            if (source.content.contains("." + method.name + "(")
                    || source.content.contains("::" + method.name)
                    || callPattern.matcher(source.content).find()) {
                return true;
            }
        }
        return false;
    }

    private static final class SourceFile {

        private final String path;
        private final String content;
        private final String code;

        private SourceFile(String path, String content, String code) {
            this.path = path;
            this.content = content;
            this.code = code;
        }
    }

    private static final class LayerMethod {

        private final String fullName;
        private final String name;
        private final String path;
        private final boolean hasLayerPublicApi;
        private final String reason;

        private LayerMethod(String fullName, String name, String path, boolean hasLayerPublicApi, String reason) {
            this.fullName = fullName;
            this.name = name;
            this.path = path;
            this.hasLayerPublicApi = hasLayerPublicApi;
            this.reason = reason;
        }
    }
}
