package com.github.thundax.modules.sys.aop;

import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.common.utils.JsonUtils;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.common.web.RequestUtils;
import com.github.thundax.modules.auth.utils.UserAccessHolder;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.utils.SysLogUtils;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

/**
 * @author wdit
 */
public class SysLogMethodInterceptor implements MethodInterceptor {

    private static final String TITLE_SEPARATOR = "-";

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {

        String[] modules = null;
        String value = null;
        String category = null;

        Method method = methodInvocation.getMethod();

        SysLogger annotation = AnnotationUtils.findAnnotation(method, SysLogger.class);
        if (annotation != null) {
            modules = annotation.module();
            value = annotation.value();
            category = annotation.category();
        }

        if (StringUtils.isEmpty(value)
                || ArrayUtils.isEmpty(modules)
                || StringUtils.isEmpty(category)) {
            Class<?> clazz = methodInvocation.getClass();
            System.out.println(clazz);

            SysLogger parentAnnotation = AnnotationUtils.findAnnotation(method.getDeclaringClass(), SysLogger.class);
            Assert.notNull(parentAnnotation, "modules of annotation '@SysLogger' is empty");

            if (StringUtils.isEmpty(value)) {
                value = parentAnnotation.value();
                Assert.notNull(value, "value of annotation '@SysLogger' is empty");
            }

            if (StringUtils.isEmpty(category)) {
                category = parentAnnotation.category();
            }

            if (ArrayUtils.isEmpty(modules)) {
                modules = parentAnnotation.module();
                Assert.notEmpty(modules, "modules of annotation '@SysLogger' is empty");
            }
        }

        Object requestBody = findRequestObjectArgument(method, methodInvocation.getArguments());

        writeLog(modules, value, category, requestBody);

        return methodInvocation.proceed();
    }

    private Object findRequestObjectArgument(Method method, Object[] arguments) {
        if (ArrayUtils.isEmpty(arguments)) {
            return null;
        }

        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int parameterIdx = 0; parameterIdx < parameterAnnotations.length; parameterIdx++) {
            Annotation[] annotations = parameterAnnotations[parameterIdx];
            if (ArrayUtils.isEmpty(annotations)) {
                continue;
            }

            for (Annotation annotation : annotations) {
                if (annotation instanceof RequestBody) {
                    return arguments[parameterIdx];
                }
            }
        }

        return null;
    }

    private void writeLog(String[] modules, String value, String category, Object requestBody) {
        HttpServletRequest currentRequest = RequestUtils.currentRequest();

        List<String> titleParts = ListUtils.newArrayList(modules);
        titleParts.add(value);

        Log log = new Log();
        log.setUserId(UserAccessHolder.currentUserId());
        log.setTitle(StringUtils.join(titleParts, TITLE_SEPARATOR));

        log.setLogDate(new Date());

        log.setRemoteAddr(RequestUtils.getRemoteAddr(currentRequest));
        log.setUserAgent(currentRequest.getHeader("user-agent"));
        log.setRequestUri(currentRequest.getRequestURI());
        log.setMethod(currentRequest.getMethod());

        if (StringUtils.isEmpty(category)) {
            log.setType(Log.TYPE_ACCESS);
        } else {
            log.setType(category);
        }

        if (requestBody != null) {
            log.setRequestParams(JsonUtils.toJson(requestBody));
        }

        SysLogUtils.saveLog(log);
    }

}
