package com.github.thundax.architecture;

import static org.junit.Assert.assertTrue;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class DaoNamingArchitectureTest extends AbstractArchitectureTest {

    @Test
    public void shouldUseDaoPortMethodShape() {
        JavaClasses classes = importPackages("com.github.thundax.modules");
        List<String> violations = new ArrayList<>();

        for (JavaClass javaClass : classes) {
            if (!isDaoInterface(javaClass)) {
                continue;
            }
            for (JavaMethod method : javaClass.getMethods()) {
                if (!isDaoPortMethodShape(method)) {
                    violations.add(method.getFullName());
                }
            }
        }

        assertTrue(
                "DAO interface methods should use getById/getByXxx/list/listByIds/page/count/deleteById/batchXxx "
                        + "naming: "
                        + violations,
                violations.isEmpty());
    }

    private boolean isDaoInterface(JavaClass javaClass) {
        return javaClass.isInterface()
                && javaClass.getSimpleName().endsWith("Dao")
                && javaClass.getPackageName().contains(".dao");
    }

    private boolean isDaoPortMethodShape(JavaMethod method) {
        String name = method.getName();
        if (isNonStandardIdsListName(name) || name.startsWith("find")) {
            return false;
        }
        return name.equals("count")
                || name.equals("list")
                || name.equals("page")
                || name.equals("deleteAll")
                || name.startsWith("getBy")
                || name.startsWith("list")
                || name.startsWith("count")
                || name.startsWith("insert")
                || name.startsWith("update")
                || name.startsWith("deleteBy")
                || name.startsWith("batch")
                || isDaoBusinessActionName(name);
    }

    private boolean isNonStandardIdsListName(String name) {
        return name.endsWith("ByIds") && !name.equals("listByIds");
    }

    private boolean isDaoBusinessActionName(String name) {
        return name.equals("active")
                || name.equals("canSend")
                || name.equals("deleteBusiness")
                || name.equals("deleteBusinessByBusiness")
                || name.equals("deleteMenuRole")
                || name.equals("deleteRoleMenu")
                || name.equals("deleteRoleUser")
                || name.equals("deleteUserRole")
                || name.equals("getContentById")
                || name.equals("getDictionaryRevision")
                || name.equals("getMultipartPart")
                || name.equals("getMultipartSessionByUploadId")
                || name.equals("getUidByToken")
                || name.equals("isChildOf")
                || name.equals("markSent")
                || name.equals("moveTreeNode")
                || name.equals("tokenExists")
                || name.equals("touch");
    }
}
