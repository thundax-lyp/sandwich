package com.github.thundax.modules.auth.security.subject;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.modules.auth.exception.RequirePermissionException;
import com.github.thundax.modules.auth.exception.RequireRoleException;

import java.util.Map;
import java.util.Set;

/**
 * @author thundax
 */
public class Subject {

    private static final String PERMISSION_SEPARATOR = ":";

    private String userId;
    private Set<String> permissions;
    private Set<String> roleIdentifiers;

    private Map<String, String> params;

    public Subject() {

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public Set<String> getRoleIdentifiers() {
        return roleIdentifiers;
    }

    public void setRoleIdentifiers(Set<String> roleIdentifiers) {
        this.roleIdentifiers = roleIdentifiers;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    /**
     * 是否提供 permission
     *
     * @param permission permission
     * @return true
     */
    public boolean isPermitted(String permission) {
        if (StringUtils.isEmpty(permission)) {
            return false;
        }

        String[] parts = StringUtils.split(permission, PERMISSION_SEPARATOR);
        String targetPermission = StringUtils.EMPTY;
        for (int idx = 0; idx < parts.length; idx++) {
            if (idx == 0) {
                targetPermission = parts[idx];
            } else {
                targetPermission = targetPermission + PERMISSION_SEPARATOR + parts[idx];
            }
            if (permissions.contains(targetPermission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查 permission
     *
     * @param permission permission
     * @throws ApiException 异常
     */
    public void checkPermission(String permission) throws ApiException {
        if (!isPermitted(permission)) {
            throw new RequirePermissionException(permission);
        }
    }

    /**
     * 检查 permissions，AND 关系
     *
     * @param permissions permissions
     * @throws ApiException 异常
     */
    public void checkPermissions(String... permissions) throws ApiException {
        for (String permission : permissions) {
            checkPermission(permission);
        }
    }

    /**
     * 是否提供 roleIdentifier
     *
     * @param roleIdentifier roleIdentifier
     * @return true
     */
    public boolean hasRole(String roleIdentifier) {
        return roleIdentifiers != null && roleIdentifiers.contains(roleIdentifier);
    }

    /**
     * 检查 roleIdentifier
     *
     * @param roleIdentifier roleIdentifier
     * @throws ApiException 异常
     */
    public void checkRole(String roleIdentifier) throws ApiException {
        if (!hasRole(roleIdentifier)) {
            throw new RequireRoleException(roleIdentifier);
        }
    }

    /**
     * 检查 roleIdentifiers，AND 关系
     *
     * @param roleIdentifiers roleIdentifiers
     * @throws ApiException 异常
     */
    public void checkRoles(String... roleIdentifiers) throws ApiException {
        for (String roleIdentifier : roleIdentifiers) {
            checkRole(roleIdentifier);
        }
    }

}
