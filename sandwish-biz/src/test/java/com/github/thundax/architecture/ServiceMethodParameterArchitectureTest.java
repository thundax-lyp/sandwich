package com.github.thundax.architecture;

import static org.junit.Assert.assertTrue;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class ServiceMethodParameterArchitectureTest extends AbstractArchitectureTest {

    private static final Set<String> LEGACY_DIRTY_SERVICE_METHOD_PARAMETERS = new LinkedHashSet<String>(Arrays.asList(
            "AsyncTaskService#add",
            "AsyncTaskService#deleteById",
            "AsyncTaskService#getById",
            "AsyncTaskService#update",
            "CurrentUserService#listAccessibleMenus",
            "CurrentUserService#listVisibleMenus",
            "CurrentUserService#updateInfo",
            "CurrentUserService#updatePassword",
            "DepartmentService#add",
            "DepartmentService#batchDeleteById",
            "DepartmentService#getById",
            "DepartmentService#isChildOf",
            "DepartmentService#listAll",
            "DepartmentService#moveTreeNode",
            "DepartmentService#update",
            "LogService#add",
            "LogService#batchDelete",
            "LogService#batchInsert",
            "LogService#deleteById",
            "LogService#getById",
            "LogService#update",
            "MemberService#add",
            "MemberService#batchDeleteById",
            "MemberService#batchUpdateStatus",
            "MemberService#deleteById",
            "MemberService#getById",
            "MemberService#listByIds",
            "MemberService#update",
            "MemberService#updateInfo",
            "MemberService#updateStatus",
            "MenuService#add",
            "MenuService#batchDeleteById",
            "MenuService#batchUpdateVisibility",
            "MenuService#getById",
            "MenuService#isChildOf",
            "MenuService#listByIds",
            "MenuService#moveTreeNode",
            "MenuService#update",
            "MenuService#updateVisibility",
            "MultipartUploadService#abortMultipartUpload",
            "MultipartUploadService#completeMultipartUpload",
            "MultipartUploadService#initMultipartUpload",
            "MultipartUploadService#uploadMultipartPart",
            "PreAuthSessionService#count",
            "PreAuthSessionService#create",
            "PreAuthSessionService#findIdByRefreshToken",
            "PreAuthSessionService#findIdByToken",
            "PreAuthSessionService#findValue",
            "PreAuthSessionService#getById",
            "PreAuthSessionService#refresh",
            "PreAuthSessionService#release",
            "PreAuthSessionService#upsertValue",
            "PrincipalAuthService#authenticateIdentity",
            "PrincipalAuthService#authenticatePassword",
            "PrincipalCredentialService#add",
            "PrincipalCredentialService#getById",
            "PrincipalCredentialService#getByIdentityIdAndType",
            "PrincipalCredentialService#getByPrincipalKeyAndType",
            "PrincipalCredentialService#listByPrincipalKeyAndStatus",
            "PrincipalCredentialService#update",
            "PrincipalCredentialService#updateStatus",
            "PrincipalCredentialService#updateVerifyState",
            "PrincipalIdentityService#add",
            "PrincipalIdentityService#getById",
            "PrincipalIdentityService#getByIdentity",
            "PrincipalIdentityService#getByPrincipalKeyAndType",
            "PrincipalIdentityService#listByPrincipalKeyAndStatus",
            "PrincipalIdentityService#update",
            "PrincipalIdentityService#updateStatus",
            "RoleService#add",
            "RoleService#batchDeleteById",
            "RoleService#batchUpdateStatus",
            "RoleService#deleteById",
            "RoleService#getById",
            "RoleService#listEnabled",
            "RoleService#listRoleMenus",
            "RoleService#listRoleUsers",
            "RoleService#update",
            "RoleService#updatePriority",
            "RoleService#updateStatus",
            "RoleService#updateUserList",
            "StorageService#add",
            "StorageService#addReferences",
            "StorageService#batchDeleteById",
            "StorageService#canReadContent",
            "StorageService#deleteById",
            "StorageService#getById",
            "StorageService#listByIds",
            "StorageService#listMimeTypes",
            "StorageService#listReferenceOwnerTypes",
            "StorageService#listReferences",
            "StorageService#removeReferences",
            "StorageService#update",
            "StorageService#updateObjectStatus",
            "StorageService#updateReferenceStatus",
            "UserService#add",
            "UserService#batchDeleteById",
            "UserService#batchUpdateStatus",
            "UserService#getById",
            "UserService#listAll",
            "UserService#listUserRoles",
            "UserService#update",
            "UserService#updateStatus"));

    @Test
    public void shouldUseQueryPageQueryOrCommandForServiceParameters() {
        JavaClasses classes = importPackages("com.github.thundax.modules");
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (!isServiceInterface(javaClass)) {
                continue;
            }
            for (JavaMethod method : javaClass.getMethods()) {
                if (LEGACY_DIRTY_SERVICE_METHOD_PARAMETERS.contains(methodKey(javaClass, method))) {
                    continue;
                }
                if (!matchesTargetShape(method)) {
                    violations.add(method.getFullName());
                }
            }
        }

        assertTrue(
                "Service methods must use one of the target parameter shapes: (*Query), (*Query, PageQuery), "
                        + "or (*Command). Legacy methods still waiting for TODO cleanup are "
                        + LEGACY_DIRTY_SERVICE_METHOD_PARAMETERS
                        + ". Violations: "
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
                || methodName.startsWith("exists");
    }

    private boolean isServiceInterface(JavaClass javaClass) {
        return javaClass.isInterface()
                && javaClass.getSimpleName().endsWith("Service")
                && javaClass.getPackageName().endsWith(".service");
    }

    private boolean isServiceQuery(JavaClass javaClass) {
        return javaClass.getSimpleName().endsWith("Query") && javaClass.getPackageName().contains(".service.query");
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

    private String methodKey(JavaClass javaClass, JavaMethod method) {
        return javaClass.getSimpleName() + "#" + method.getName();
    }
}
