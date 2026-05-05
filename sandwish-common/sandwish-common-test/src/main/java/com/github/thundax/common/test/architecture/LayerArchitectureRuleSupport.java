package com.github.thundax.common.test.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.Assert.assertTrue;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.ArchRule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class LayerArchitectureRuleSupport {

    private static final Set<String> DEFAULT_LEGACY_SERVICE_BOUNDARY_DIRTY_TYPES = Collections.emptySet();

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
}
