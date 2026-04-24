package com.github.thundax.modules.auth.security.aop;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.modules.auth.security.SecurityUtils;
import com.github.thundax.modules.auth.security.annotation.Logical;
import com.github.thundax.modules.auth.security.annotation.RequiresRoles;
import com.github.thundax.modules.auth.security.subject.Subject;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.util.Assert;

/**
 * @author wdit
 */
public class RoleAnnotationMethodInterceptor implements AnnotationMethodInterceptor {

    @Override
    public boolean supports(MethodInvocation methodInvocation) {
        return methodInvocation.getMethod().getAnnotation(RequiresRoles.class) != null;
    }

    @Override
    public void assertAuthorized(MethodInvocation methodInvocation) throws ApiException {
        RequiresRoles annotation = methodInvocation.getMethod().getAnnotation(RequiresRoles.class);
        Assert.notNull(annotation, "required annotation 'RequiresRoles'");

        String[] roleIdentifiers = annotation.value();
        Assert.notEmpty(roleIdentifiers, "value of annotation 'RequiresRoles' is empty");

        Subject subject = SecurityUtils.getSubject();
        Assert.notNull(subject, "Subject can not be null");

        if (roleIdentifiers.length == 1) {
            subject.checkRole(roleIdentifiers[0]);
            return;
        }

        if (Logical.AND.equals(annotation.logical())) {
            subject.checkRoles(roleIdentifiers);
            return;
        }

        if (Logical.OR.equals(annotation.logical())) {
            boolean hasAtLeastOneRoleIdentifier = false;
            for (String roleIdentifier : roleIdentifiers) {
                if (subject.hasRole(roleIdentifier)) {
                    hasAtLeastOneRoleIdentifier = true;
                }
            }

            if (!hasAtLeastOneRoleIdentifier) {
                subject.checkRole(roleIdentifiers[0]);
            }
        }
    }

}

