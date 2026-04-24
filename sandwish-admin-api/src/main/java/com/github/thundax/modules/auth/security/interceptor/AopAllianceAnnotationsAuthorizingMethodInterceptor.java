package com.github.thundax.modules.auth.security.interceptor;

import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.modules.auth.security.aop.AnnotationMethodInterceptor;
import com.github.thundax.modules.auth.security.aop.PermissionAnnotationMethodInterceptor;
import com.github.thundax.modules.auth.security.aop.RoleAnnotationMethodInterceptor;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.util.List;

/**
 * recode from shiro
 *
 * @author thundax
 */
public class AopAllianceAnnotationsAuthorizingMethodInterceptor implements MethodInterceptor {

    private final List<AnnotationMethodInterceptor> interceptors = ListUtils.newArrayList();

    public AopAllianceAnnotationsAuthorizingMethodInterceptor() {
        interceptors.add(new PermissionAnnotationMethodInterceptor());
        interceptors.add(new RoleAnnotationMethodInterceptor());
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        for (AnnotationMethodInterceptor interceptor : interceptors) {
            if (interceptor.supports(methodInvocation)) {
                interceptor.assertAuthorized(methodInvocation);
            }
        }
        return methodInvocation.proceed();
    }

}
