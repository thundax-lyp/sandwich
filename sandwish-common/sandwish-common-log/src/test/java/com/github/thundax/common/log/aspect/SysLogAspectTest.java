package com.github.thundax.common.log.aspect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.github.thundax.common.log.annotation.SysLog;
import com.github.thundax.common.log.model.SysLogEvent;
import com.github.thundax.common.log.producer.SysLogProducer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Test;

public class SysLogAspectTest {

    @Test
    public void shouldProduceSuccessEvent() throws Throwable {
        CapturingSysLogProducer producer = new CapturingSysLogProducer();
        SysLogAspect aspect = new SysLogAspect(producer);
        Method method = FixtureService.class.getMethod("success");
        ProceedingJoinPoint joinPoint = joinPoint(method, "ok", null);

        Object result = aspect.around(joinPoint, method.getAnnotation(SysLog.class));

        assertEquals("ok", result);
        SysLogEvent event = producer.event;
        assertEquals("create user", event.getAction());
        assertEquals("USER", event.getType());
        assertEquals(FixtureService.class.getName(), event.getClassName());
        assertEquals("success", event.getMethodName());
        assertTrue(event.isSuccess());
        assertNotNull(event.getOccurTime());
    }

    @Test
    public void shouldProduceFailedEvent() throws Throwable {
        CapturingSysLogProducer producer = new CapturingSysLogProducer();
        SysLogAspect aspect = new SysLogAspect(producer);
        Method method = FixtureService.class.getMethod("failure");
        RuntimeException failure = new RuntimeException("boom");
        ProceedingJoinPoint joinPoint = joinPoint(method, null, failure);

        try {
            aspect.around(joinPoint, method.getAnnotation(SysLog.class));
        } catch (RuntimeException ex) {
            assertEquals(failure, ex);
        }

        SysLogEvent event = producer.event;
        assertFalse(event.isSuccess());
        assertEquals("boom", event.getErrorMessage());
    }

    private ProceedingJoinPoint joinPoint(final Method method, final Object result, final Throwable failure) {
        final MethodSignature signature = methodSignature(method);
        return (ProceedingJoinPoint) Proxy.newProxyInstance(
                ProceedingJoinPoint.class.getClassLoader(),
                new Class<?>[] {ProceedingJoinPoint.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method invokedMethod, Object[] args) throws Throwable {
                        String methodName = invokedMethod.getName();
                        if ("getSignature".equals(methodName)) {
                            return signature;
                        }
                        if ("proceed".equals(methodName)) {
                            if (failure != null) {
                                throw failure;
                            }
                            return result;
                        }
                        if ("toString".equals(methodName)) {
                            return "ProceedingJoinPointStub";
                        }
                        return defaultValue(invokedMethod.getReturnType());
                    }
                });
    }

    private MethodSignature methodSignature(final Method method) {
        return (MethodSignature) Proxy.newProxyInstance(
                MethodSignature.class.getClassLoader(),
                new Class<?>[] {MethodSignature.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method invokedMethod, Object[] args) {
                        String methodName = invokedMethod.getName();
                        if ("getMethod".equals(methodName)) {
                            return method;
                        }
                        if ("getDeclaringTypeName".equals(methodName)) {
                            return FixtureService.class.getName();
                        }
                        if ("toString".equals(methodName)) {
                            return "MethodSignatureStub";
                        }
                        return defaultValue(invokedMethod.getReturnType());
                    }
                });
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (Boolean.TYPE.equals(type)) {
            return false;
        }
        if (Integer.TYPE.equals(type)) {
            return 0;
        }
        if (Long.TYPE.equals(type)) {
            return 0L;
        }
        if (Void.TYPE.equals(type)) {
            return null;
        }
        return 0;
    }

    private static class CapturingSysLogProducer implements SysLogProducer {

        private SysLogEvent event;

        @Override
        public void produce(SysLogEvent event) {
            this.event = event;
        }
    }

    private static class FixtureService {

        @SysLog(value = "create user", type = "USER")
        public void success() {}

        @SysLog(value = "delete user", type = "USER")
        public void failure() {}
    }
}
