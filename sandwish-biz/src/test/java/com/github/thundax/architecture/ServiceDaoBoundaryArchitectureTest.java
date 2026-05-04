package com.github.thundax.architecture;

import static org.junit.Assert.assertTrue;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class ServiceDaoBoundaryArchitectureTest extends AbstractArchitectureTest {

    private static final Set<String> LEGACY_SERVICE_BOUNDARY_DIRTY_TYPES = java.util.Collections.emptySet();

    @Test
    public void shouldKeepServiceBoundaryTypesCleanExceptKnownDirtyTypes() {
        JavaClasses classes = importPackages("com.github.thundax.modules");
        List<String> violations = new ArrayList<>();

        for (JavaClass javaClass : classes) {
            if (!isServiceInterface(javaClass)) {
                continue;
            }
            for (JavaMethod method : javaClass.getMethods()) {
                collectServiceReturnTypeViolation(method, method.getRawReturnType(), violations);
                for (JavaClass parameterType : method.getRawParameterTypes()) {
                    collectServiceParameterTypeViolation(method, parameterType, violations);
                }
            }
        }

        assertTrue(
                "Service method parameters must be *DTO, *Query, Entity, or Java-Type, and return values must be "
                        + "*DTO, Entity, or Java-Type. Known dirty types "
                        + "that still need cleanup are "
                        + LEGACY_SERVICE_BOUNDARY_DIRTY_TYPES
                        + ". New violations: "
                        + violations,
                violations.isEmpty());
    }

    @Test
    public void shouldKeepDaoBoundaryTypesClean() {
        JavaClasses classes = importPackages("com.github.thundax.modules");
        List<String> violations = new ArrayList<>();

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

    @Test
    public void shouldNotIntroduceEmptyServiceBaseTypes() {
        JavaClasses classes = importPackages("com.github.thundax.modules");
        List<String> violations = new ArrayList<>();

        for (JavaClass javaClass : classes) {
            if (!isEmptyServiceBaseType(javaClass)) {
                continue;
            }
            violations.add(javaClass.getName());
        }

        assertTrue(
                "Empty BaseService/BaseServiceImpl style types have no Service boundary value: " + violations,
                violations.isEmpty());
    }

    private void collectServiceReturnTypeViolation(JavaMethod method, JavaClass type, List<String> violations) {
        if (isAllowedServiceReturnType(type) || LEGACY_SERVICE_BOUNDARY_DIRTY_TYPES.contains(type.getName())) {
            return;
        }
        violations.add(method.getFullName() + " has invalid return type " + type.getName());
    }

    private void collectServiceParameterTypeViolation(JavaMethod method, JavaClass type, List<String> violations) {
        if (isAllowedServiceParameterType(type) || LEGACY_SERVICE_BOUNDARY_DIRTY_TYPES.contains(type.getName())) {
            return;
        }
        violations.add(method.getFullName() + " has invalid parameter type " + type.getName());
    }

    private void collectDaoReturnViolation(JavaMethod method, JavaClass type, List<String> violations) {
        if (isAllowedDaoResultType(method, type)) {
            return;
        }
        violations.add(method.getFullName() + " has invalid return type " + type.getName());
    }

    private void collectDaoParameterViolation(JavaMethod method, JavaClass type, List<String> violations) {
        if (isAllowedDaoParameterType(type)) {
            return;
        }
        violations.add(method.getFullName() + " has invalid parameter type " + type.getName());
    }

    private boolean isAllowedServiceReturnType(JavaClass type) {
        return isVoid(type) || isJavaType(type) || isModuleEntity(type) || isDto(type);
    }

    private boolean isAllowedServiceParameterType(JavaClass type) {
        return isAllowedServiceReturnType(type) || isServiceQuery(type);
    }

    private boolean isAllowedDaoResultType(JavaMethod method, JavaClass type) {
        return isAllowedDaoParameterType(type) || isMyBatisPlusPage(type) && "page".equals(method.getName());
    }

    private boolean isAllowedDaoParameterType(JavaClass type) {
        return isJavaType(type) || isModuleEntity(type);
    }

    private boolean isServiceInterface(JavaClass javaClass) {
        return javaClass.isInterface()
                && javaClass.getSimpleName().endsWith("Service")
                && javaClass.getPackageName().contains(".service");
    }

    private boolean isDaoInterface(JavaClass javaClass) {
        return javaClass.isInterface()
                && javaClass.getSimpleName().endsWith("Dao")
                && javaClass.getPackageName().contains(".dao");
    }

    private boolean isVoid(JavaClass type) {
        return "void".equals(type.getName()) || "java.lang.Void".equals(type.getName());
    }

    private boolean isJavaType(JavaClass type) {
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

    private boolean isModuleEntity(JavaClass type) {
        return type.getPackageName().contains(".modules.")
                && type.getPackageName().contains(".entity");
    }

    private boolean isDto(JavaClass type) {
        return type.getSimpleName().endsWith("DTO");
    }

    private boolean isServiceQuery(JavaClass type) {
        return type.getSimpleName().endsWith("Query") && type.getPackageName().contains(".service.query");
    }

    private boolean isMyBatisPlusPage(JavaClass type) {
        return "com.baomidou.mybatisplus.extension.plugins.pagination.Page".equals(type.getName());
    }

    private boolean isEmptyServiceBaseType(JavaClass javaClass) {
        return javaClass.getSimpleName().equals("BaseService")
                || javaClass.getSimpleName().equals("BaseServiceImpl")
                || javaClass.getSimpleName().endsWith("BaseService")
                || javaClass.getSimpleName().endsWith("BaseServiceImpl");
    }
}
