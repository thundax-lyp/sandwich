package com.github.thundax.architecture;

import static org.junit.Assert.assertTrue;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.github.thundax.common.test.architecture.NamingArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class ServiceNamingArchitectureTest extends AbstractArchitectureTest {

    private static final Set<String> LEGACY_DIRTY_SERVICE_METHOD_NAMES = new LinkedHashSet<String>(Arrays.asList(
            "AsyncTaskService#deleteById",
            "AsyncTaskService#update",
            "CurrentUserService#updateInfo",
            "CurrentUserService#updatePassword",
            "DepartmentService#batchDeleteById",
            "DepartmentService#update",
            "DictService#batchDeleteById",
            "DictService#update",
            "LogService#batchDelete",
            "LogService#batchInsert",
            "LogService#deleteById",
            "LogService#update",
            "MemberService#batchDeleteById",
            "MemberService#batchUpdateStatus",
            "MemberService#deleteById",
            "MemberService#update",
            "MemberService#updateInfo",
            "MemberService#updateStatus",
            "MenuService#batchDeleteById",
            "MenuService#batchUpdateVisibility",
            "MenuService#update",
            "MenuService#updateVisibility",
            "PrincipalCredentialService#update",
            "PrincipalCredentialService#updateStatus",
            "PrincipalCredentialService#updateVerifyState",
            "PrincipalIdentityService#update",
            "PrincipalIdentityService#updateStatus",
            "RoleService#batchDeleteById",
            "RoleService#batchUpdateStatus",
            "RoleService#deleteById",
            "RoleService#update",
            "RoleService#updatePriority",
            "RoleService#updateStatus",
            "RoleService#updateUserList",
            "StorageService#batchDeleteById",
            "StorageService#deleteById",
            "StorageService#update",
            "StorageService#updateObjectStatus",
            "StorageService#updateReferenceStatus",
            "UserService#batchDeleteById",
            "UserService#batchUpdateStatus",
            "UserService#update",
            "UserService#updateStatus"));

    @Test
    public void shouldReturnCreatedEntityIdFromServiceAddMethods() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        NamingArchitectureRuleSupport.assertServiceAddMethodsReturnEntityId(classes);
    }

    @Test
    public void shouldNotOverloadServiceInterfaceMethods() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        NamingArchitectureRuleSupport.assertServiceInterfaceMethodsAreNotOverloaded(classes);
    }

    @Test
    public void shouldUseBusinessActionNamesForServiceMethods() {
        JavaClasses classes = importPackages("com.github.thundax.modules");
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (!isServiceInterface(javaClass)) {
                continue;
            }
            for (JavaMethod method : javaClass.getMethods()) {
                if (LEGACY_DIRTY_SERVICE_METHOD_NAMES.contains(methodKey(javaClass, method))) {
                    continue;
                }
                if (isForbiddenServiceMethodName(method.getName())) {
                    violations.add(method.getFullName());
                }
            }
        }

        assertTrue(
                "Service method names must express business actions and must not use update/save/insert/batch "
                        + "or deleteById style names. Legacy methods still waiting for TODO cleanup are "
                        + LEGACY_DIRTY_SERVICE_METHOD_NAMES
                        + ". Violations: "
                        + violations,
                violations.isEmpty());
    }

    @Test
    public void shouldKeepUserServiceIdentityAndCredentialBoundaries() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        NamingArchitectureRuleSupport.assertUserServiceDoesNotExposeIdentityOrCredentialMethods(classes);
    }

    @Test
    public void shouldPlaceServiceQueryObjectsUnderServiceQueryPackage() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        NamingArchitectureRuleSupport.assertServiceQueryObjectsUnderServiceQueryPackage(classes);
    }

    @Test
    public void shouldNotDeclareSettersInServiceQueryObjects() throws IOException {
        Path root = repositoryRoot();
        Path sourceRoot = root.resolve("sandwish-biz").resolve("src/main/java/com/github/thundax/modules");

        NamingArchitectureRuleSupport.assertServiceQueryObjectsDeclareNoSetters(sourceRoot);
    }

    @Test
    public void shouldDeclareOnlyRequiredAnnotationsInServiceQueryObjects() throws IOException {
        Path root = repositoryRoot();
        Path sourceRoot = root.resolve("sandwish-biz").resolve("src/main/java/com/github/thundax/modules");

        NamingArchitectureRuleSupport.assertServiceQueryObjectsDeclareOnlyRequiredAnnotations(sourceRoot);
    }

    private Path repositoryRoot() {
        Path current = Paths.get("").toAbsolutePath();
        if (Files.exists(current.resolve("TODO.md"))) {
            return current;
        }
        Path parent = current.getParent();
        while (parent != null) {
            if (Files.exists(parent.resolve("TODO.md"))) {
                return parent;
            }
            parent = parent.getParent();
        }
        throw new IllegalStateException("Can not find repository root");
    }

    private boolean isServiceInterface(JavaClass javaClass) {
        return javaClass.isInterface()
                && javaClass.getSimpleName().endsWith("Service")
                && javaClass.getPackageName().endsWith(".service");
    }

    private boolean isForbiddenServiceMethodName(String name) {
        return name.startsWith("batch")
                || name.startsWith("insert")
                || name.startsWith("save")
                || name.startsWith("update")
                || name.startsWith("modify")
                || name.startsWith("handle")
                || name.startsWith("operate")
                || name.startsWith("process")
                || name.startsWith("execute")
                || name.startsWith("submit")
                || name.startsWith("manage")
                || name.startsWith("maintain")
                || name.startsWith("select")
                || "do".equals(name)
                || "deleteById".equals(name);
    }

    private String methodKey(JavaClass javaClass, JavaMethod method) {
        return javaClass.getSimpleName() + "#" + method.getName();
    }
}
