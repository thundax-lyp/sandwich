package com.github.thundax.modules.auth.security.aop;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.modules.auth.security.SecurityUtils;
import com.github.thundax.modules.auth.security.annotation.Logical;
import com.github.thundax.modules.auth.security.annotation.RequiresPermissions;
import com.github.thundax.modules.auth.security.subject.Subject;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.util.Assert;

/**
 * @author wdit
 */
public class PermissionAnnotationMethodInterceptor implements AnnotationMethodInterceptor {

    @Override
    public boolean supports(MethodInvocation methodInvocation) {
        return methodInvocation.getMethod().getAnnotation(RequiresPermissions.class) != null;
    }

    @Override
    public void assertAuthorized(MethodInvocation methodInvocation) throws ApiException {
        RequiresPermissions annotation = methodInvocation.getMethod().getAnnotation(RequiresPermissions.class);
        Assert.notNull(annotation, "required annotation 'RequiresPermissions'");

        String[] perms = annotation.value();
        Assert.notEmpty(perms, "value of annotation 'RequiresPermissions' is empty");

        Subject subject = SecurityUtils.getSubject();
        Assert.notNull(subject, "Subject can not be null");

        if (perms.length == 1) {
            subject.checkPermission(perms[0]);
            return;
        }

        if (Logical.AND.equals(annotation.logical())) {
            subject.checkPermissions(perms);
            return;
        }

        if (Logical.OR.equals(annotation.logical())) {
            boolean hasAtLeastOnePermission = false;
            for (String permission : perms) {
                if (subject.isPermitted(permission)) {
                    hasAtLeastOnePermission = true;
                }
            }

            if (!hasAtLeastOnePermission) {
                subject.checkPermission(perms[0]);
            }
        }
    }

}

