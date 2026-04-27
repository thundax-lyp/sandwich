package com.github.thundax.modules.sys.aop;

import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/** @author wdit */
@SuppressWarnings({"unchecked"})
public class SysLogPointcutAdvisor extends StaticMethodMatcherPointcutAdvisor {

    private static final Class<? extends Annotation>[] ANNOTATION_CLASSES =
            new Class[] {SysLogger.class};

    public SysLogPointcutAdvisor() {
        setAdvice(new SysLogMethodInterceptor());
    }

    @Override
    public boolean matches(@NonNull Method method, @Nullable Class targetClass) {
        Method m = method;

        if (isAnnotationPresent(m)) {
            return true;
        }

        if (targetClass != null) {
            try {
                m = targetClass.getMethod(m.getName(), m.getParameterTypes());
                return isAnnotationPresent(m);

            } catch (NoSuchMethodException ignored) {
                // default return value is false.  If we can't find the method, then obviously
                // there is no annotation, so just use the default return value.
            }
        }

        return false;
    }

    private boolean isAnnotationPresent(Class<?> targetClazz) {
        for (Class<? extends Annotation> annClass : ANNOTATION_CLASSES) {
            Annotation a = AnnotationUtils.findAnnotation(targetClazz, annClass);
            if (a != null) {
                return true;
            }
        }
        return false;
    }

    private boolean isAnnotationPresent(Method method) {
        for (Class<? extends Annotation> annClass : ANNOTATION_CLASSES) {
            Annotation a = AnnotationUtils.findAnnotation(method, annClass);
            if (a != null) {
                return true;
            }
        }
        return false;
    }
}
