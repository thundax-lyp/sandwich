package com.github.thundax.architecture;

import static org.junit.Assert.assertTrue;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class ServiceMethodParameterArchitectureTest extends AbstractArchitectureTest {

    @Test
    public void shouldUseQueryPageQueryOrCommandForServiceParameters() {
        JavaClasses classes = importPackages("com.github.thundax.modules");
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (!isServiceInterface(javaClass)) {
                continue;
            }
            for (JavaMethod method : javaClass.getMethods()) {
                if (!matchesTargetShape(method)) {
                    violations.add(method.getFullName());
                }
            }
        }

        assertTrue(
                "Service methods must use one of the target parameter shapes: (*Query), (*Query, PageQuery), "
                        + "or (*Command). Violations: "
                        + violations,
                violations.isEmpty());
    }

    private boolean matchesTargetShape(JavaMethod method) {
        List<JavaClass> parameters = new ArrayList<JavaClass>(method.getRawParameterTypes());
        if ("page".equals(method.getName())) {
            return parameters.size() == 2
                    && isServiceQuery(parameters.get(0))
                    && isPageQuery(parameters.get(1))
                    && isPageResult(method.getRawReturnType());
        }
        if (isQueryMethod(method.getName())) {
            return parameters.size() == 1 && isServiceQuery(parameters.get(0));
        }
        return parameters.size() == 1 && isServiceCommand(parameters.get(0));
    }

    private boolean isQueryMethod(String methodName) {
        return methodName.startsWith("get")
                || methodName.startsWith("list")
                || methodName.startsWith("count")
                || methodName.startsWith("exists")
                || methodName.startsWith("deleteBy");
    }

    private boolean isServiceInterface(JavaClass javaClass) {
        return javaClass.isInterface()
                && javaClass.getSimpleName().endsWith("Service")
                && javaClass.getPackageName().endsWith(".service");
    }

    private boolean isServiceQuery(JavaClass javaClass) {
        return javaClass.getSimpleName().endsWith("Query")
                && javaClass.getPackageName().contains(".service.query");
    }

    private boolean isServiceCommand(JavaClass javaClass) {
        return javaClass.getSimpleName().endsWith("Command")
                && javaClass.getPackageName().contains(".service.command");
    }

    private boolean isPageQuery(JavaClass javaClass) {
        return "com.github.thundax.common.page.PageQuery".equals(javaClass.getName());
    }

    private boolean isPageResult(JavaClass javaClass) {
        return "com.github.thundax.common.page.PageResult".equals(javaClass.getName());
    }

}
