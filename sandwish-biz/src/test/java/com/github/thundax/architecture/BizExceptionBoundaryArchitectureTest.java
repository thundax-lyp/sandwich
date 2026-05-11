package com.github.thundax.architecture;

import static org.junit.Assert.assertTrue;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.github.thundax.modules.exception.BizExceptionBoundary;
import com.github.thundax.modules.exception.BizExceptionBoundaryIgnore;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class BizExceptionBoundaryArchitectureTest extends AbstractArchitectureTest {

    @Test
    public void shouldDeclareBizExceptionBoundaryOnServiceImplementationClasses() {
        JavaClasses classes = importPackages("com.github.thundax.modules");
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (!isServiceImplementation(javaClass)) {
                continue;
            }
            if (hasBoundaryMethod(javaClass) && !javaClass.isAnnotatedWith(BizExceptionBoundary.class)) {
                violations.add(javaClass.getFullName());
            }
        }

        assertTrue(
                "Service implementations with non-getter public methods must declare @BizExceptionBoundary: "
                        + violations,
                violations.isEmpty());
    }

    @Test
    public void shouldOnlyIgnoreGetterMethodsFromBizExceptionBoundary() {
        JavaClasses classes = importPackages("com.github.thundax.modules");
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            for (JavaMethod method : javaClass.getMethods()) {
                if (method.isAnnotatedWith(BizExceptionBoundaryIgnore.class) && !isGetter(method)) {
                    violations.add(method.getFullName());
                }
            }
        }

        assertTrue(
                "@BizExceptionBoundaryIgnore only supports no-argument getter methods: " + violations,
                violations.isEmpty());
    }

    private boolean isServiceImplementation(JavaClass javaClass) {
        return javaClass.getSimpleName().endsWith("ServiceImpl")
                && javaClass.getPackageName().contains(".service.impl");
    }

    private boolean hasBoundaryMethod(JavaClass javaClass) {
        for (JavaMethod method : javaClass.getMethods()) {
            if (requiresBizExceptionBoundary(javaClass, method)) {
                return true;
            }
        }
        return false;
    }

    private boolean requiresBizExceptionBoundary(JavaClass owner, JavaMethod method) {
        return method.getOwner().equals(owner)
                && method.getModifiers().contains(JavaModifier.PUBLIC)
                && !isGetter(method);
    }

    private boolean isGetter(JavaMethod method) {
        return method.getName().startsWith("get")
                && method.getRawParameterTypes().isEmpty()
                && !"void".equals(method.getRawReturnType().getName());
    }
}
