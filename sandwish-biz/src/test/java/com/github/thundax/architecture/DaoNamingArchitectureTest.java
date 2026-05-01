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

public class DaoNamingArchitectureTest extends AbstractArchitectureTest {

    private static final Set<String> LEGACY_DAO_METHOD_NAMES =
            new HashSet<>(Arrays.asList("get", "getMany", "find", "findList", "findPage", "delete"));

    @Test
    public void shouldUseDaoPortMethodShape() {
        JavaClasses classes = importPackages("com.github.thundax.modules");
        List<String> violations = new ArrayList<>();

        for (JavaClass javaClass : classes) {
            if (!isDaoInterface(javaClass)) {
                continue;
            }
            for (JavaMethod method : javaClass.getMethods()) {
                if (LEGACY_DAO_METHOD_NAMES.contains(method.getName())) {
                    violations.add(method.getFullName());
                }
            }
        }

        assertTrue(
                "DAO interface methods must use getById/getByXxx/list/page/count/deleteById/batchXxx naming: "
                        + violations,
                violations.isEmpty());
    }

    private boolean isDaoInterface(JavaClass javaClass) {
        return javaClass.isInterface()
                && javaClass.getSimpleName().endsWith("Dao")
                && javaClass.getPackageName().contains(".dao");
    }
}
