package com.github.thundax.architecture;

import static org.junit.Assert.assertTrue;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.github.thundax.modules.exception.BizExceptionBoundary;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class BizExceptionBoundaryArchitectureTest extends AbstractArchitectureTest {

    @Test
    public void shouldDeclareBizExceptionBoundaryOnServiceImplementationMethods() {
        JavaClasses classes = importPackages("com.github.thundax.modules");
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (!isServiceImplementation(javaClass)) {
                continue;
            }
            for (JavaMethod method : javaClass.getMethods()) {
                if (requiresBizExceptionBoundary(javaClass, method)
                        && !method.isAnnotatedWith(BizExceptionBoundary.class)) {
                    violations.add(method.getFullName());
                }
            }
        }

        assertTrue(
                "Non-getter public methods in Service implementations must declare @BizExceptionBoundary: "
                        + violations,
                violations.isEmpty());
    }

    private boolean isServiceImplementation(JavaClass javaClass) {
        return javaClass.getSimpleName().endsWith("ServiceImpl")
                && javaClass.getPackageName().contains(".service.impl");
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
