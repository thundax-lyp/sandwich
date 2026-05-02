package com.github.thundax.architecture;

import static org.junit.Assert.assertTrue;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class ServiceNamingArchitectureTest extends AbstractArchitectureTest {

    private static final Set<String> LEGACY_SERVICE_METHOD_NAMES =
            new HashSet<>(Arrays.asList("get", "getMany", "find", "findList", "findPage", "findOne", "delete"));

    @Test
    public void shouldUseServiceMethodShape() {
        JavaClasses classes = importPackages("com.github.thundax.common.service", "com.github.thundax.modules");
        List<String> violations = new ArrayList<>();

        for (JavaClass javaClass : classes) {
            if (!isServiceInterface(javaClass)) {
                continue;
            }
            for (JavaMethod method : javaClass.getMethods()) {
                if (isLegacyServiceMethod(method)) {
                    violations.add(method.getFullName());
                }
            }
        }

        assertTrue(
                "Service methods must use getById/getByXxx/list/page/count/deleteById/batchXxx for generic access "
                        + "and business verbs for workflows: "
                        + violations,
                violations.isEmpty());
    }

    @Test
    public void shouldPlaceServiceQueryObjectsUnderServiceQueryPackage() {
        JavaClasses classes = importPackages("com.github.thundax.modules");
        List<String> violations = new ArrayList<>();

        for (JavaClass javaClass : classes) {
            if (isServiceQueryObject(javaClass) && !isInServiceQueryPackage(javaClass)) {
                violations.add(javaClass.getName());
            }
        }

        assertTrue(
                "Service query objects named XxxQuery must be placed under "
                        + "com.github.thundax.modules.{module}.service.query: "
                        + violations,
                violations.isEmpty());
    }

    private boolean isServiceInterface(JavaClass javaClass) {
        return javaClass.isInterface()
                && javaClass.getSimpleName().endsWith("Service")
                && javaClass.getPackageName().contains(".service");
    }

    private boolean isLegacyServiceMethod(JavaMethod method) {
        return LEGACY_SERVICE_METHOD_NAMES.contains(method.getName())
                || method.getName().startsWith("find");
    }

    private boolean isServiceQueryObject(JavaClass javaClass) {
        return javaClass.getSimpleName().endsWith("Query") && !"Query".equals(javaClass.getSimpleName());
    }

    private boolean isInServiceQueryPackage(JavaClass javaClass) {
        return javaClass.getPackageName().contains(".service.query");
    }
}
