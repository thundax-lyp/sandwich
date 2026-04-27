package com.github.thundax.common.security.permission;

import java.util.Collection;

/** 匹配项目权限编码。 */
public interface PermissionMatcher {

    boolean matches(Collection<String> permissions, String requiredPermission);

    boolean matches(String permission, String requiredPermission);
}
