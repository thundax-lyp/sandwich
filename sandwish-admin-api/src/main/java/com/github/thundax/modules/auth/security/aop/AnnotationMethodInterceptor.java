package com.github.thundax.modules.auth.security.aop;

import com.github.thundax.common.exception.ApiException;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author wdit
 */
public interface AnnotationMethodInterceptor {

    /**
     * 是否支持方法
     *
     * @param methodInvocation 方法调用
     * @return true if supports
     */
    boolean supports(MethodInvocation methodInvocation);

    /**
     * 判断权限
     *
     * @param methodInvocation methodInvocation
     * @throws ApiException 异常
     */
    void assertAuthorized(MethodInvocation methodInvocation) throws ApiException;

}
