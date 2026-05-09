package com.github.thundax.architecture;

import static org.junit.Assert.assertTrue;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class ServiceMethodModelArchitectureTest extends AbstractArchitectureTest {

    @Test
    public void shouldPlaceServiceCommandObjectsUnderServiceCommandPackage() {
        JavaClasses classes = importPackages("com.github.thundax.modules");
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (isServiceCommand(javaClass) && !javaClass.getPackageName().contains(".service.command")) {
                violations.add(javaClass.getName());
            }
        }

        assertTrue(
                "Service command objects must be placed under "
                        + "com.github.thundax.modules.{module}.service.command: "
                        + violations,
                violations.isEmpty());
    }

    @Test
    public void shouldKeepQueryCommandAndPageModelsSeparated() {
        JavaClasses classes = importPackages("com.github.thundax.modules");
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (javaClass.getPackageName().contains(".service.query")
                    && javaClass.getSimpleName().endsWith("Command")) {
                violations.add(javaClass.getName());
            }
            if (javaClass.getPackageName().contains(".service.command")
                    && javaClass.getSimpleName().endsWith("Query")) {
                violations.add(javaClass.getName());
            }
            if (javaClass.getPackageName().contains(".service.query")
                    && (javaClass.getSimpleName().equals("PageQuery")
                            || javaClass.getSimpleName().equals("PageResult"))) {
                violations.add(javaClass.getName());
            }
        }

        assertTrue(
                "Query, Command, PageQuery and PageResult must keep separate model responsibilities: " + violations,
                violations.isEmpty());
    }

    private boolean isServiceCommand(JavaClass javaClass) {
        return javaClass.getSimpleName().endsWith("Command") && !"Command".equals(javaClass.getSimpleName());
    }
}
