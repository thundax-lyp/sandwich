package com.github.thundax.architecture;

import static org.junit.Assert.assertTrue;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class HelperNamingArchitectureTest extends AbstractArchitectureTest {

    private static final String GENERIC_HELPER_NAMES = "(List|Object|Data|Common|Base|Generic)Helper";
    private static final String ARCHITECTURE_ROLE_SUFFIXES =
            ".*(Mapper|Converter|Assembler|DAO|Service|Controller|Repository|Facade|Gateway|Adapter|Client|Handler"
                    + "|Processor|Manager|Factory)";

    @Test
    public void shouldKeepHelperNamesBounded() {
        JavaClasses classes = importPackages("com.github.thundax.common");
        List<String> violations = new ArrayList<>();

        for (JavaClass javaClass : classes) {
            if (javaClass.getSimpleName().matches(GENERIC_HELPER_NAMES)) {
                violations.add(javaClass.getName());
            }
        }

        assertTrue("Helper names must bind a concrete boundary: " + violations, violations.isEmpty());
    }

    @Test
    public void shouldKeepToolPackagesOutOfArchitectureRoleNames() {
        JavaClasses classes = importPackages("com.github.thundax.common");
        List<String> violations = new ArrayList<>();

        for (JavaClass javaClass : classes) {
            if (isToolPackage(javaClass) && javaClass.getSimpleName().matches(ARCHITECTURE_ROLE_SUFFIXES)) {
                violations.add(javaClass.getName());
            }
        }

        assertTrue("Tool packages must not use architecture role suffixes: " + violations, violations.isEmpty());
    }

    private boolean isToolPackage(JavaClass javaClass) {
        String packageName = javaClass.getPackageName();
        return packageName.contains(".utils")
                || packageName.contains(".collection")
                || packageName.contains(".web.request")
                || packageName.contains(".web.response");
    }
}
