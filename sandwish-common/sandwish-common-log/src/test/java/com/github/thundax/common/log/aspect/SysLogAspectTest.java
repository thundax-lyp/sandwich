package com.github.thundax.common.log.aspect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.github.thundax.common.log.annotation.SysLog;
import com.github.thundax.common.log.model.SysLogEvent;
import com.github.thundax.common.log.producer.SysLogProducer;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class SysLogAspectTest {

    @Test
    public void shouldProduceSuccessEvent() throws Throwable {
        SysLogProducer producer = Mockito.mock(SysLogProducer.class);
        SysLogAspect aspect = new SysLogAspect(producer);
        Method method = FixtureService.class.getMethod("success");
        ProceedingJoinPoint joinPoint = joinPoint(method, "ok", null);

        Object result = aspect.around(joinPoint, method.getAnnotation(SysLog.class));

        assertEquals("ok", result);
        SysLogEvent event = capturedEvent(producer);
        assertEquals("create user", event.getAction());
        assertEquals("USER", event.getType());
        assertEquals(FixtureService.class.getName(), event.getClassName());
        assertEquals("success", event.getMethodName());
        assertTrue(event.isSuccess());
        assertNotNull(event.getOccurTime());
    }

    @Test
    public void shouldProduceFailedEvent() throws Throwable {
        SysLogProducer producer = Mockito.mock(SysLogProducer.class);
        SysLogAspect aspect = new SysLogAspect(producer);
        Method method = FixtureService.class.getMethod("failure");
        RuntimeException failure = new RuntimeException("boom");
        ProceedingJoinPoint joinPoint = joinPoint(method, null, failure);

        try {
            aspect.around(joinPoint, method.getAnnotation(SysLog.class));
        } catch (RuntimeException ex) {
            assertEquals(failure, ex);
        }

        SysLogEvent event = capturedEvent(producer);
        assertFalse(event.isSuccess());
        assertEquals("boom", event.getErrorMessage());
    }

    private ProceedingJoinPoint joinPoint(Method method, Object result, Throwable failure) throws Throwable {
        MethodSignature signature = Mockito.mock(MethodSignature.class);
        Mockito.when(signature.getMethod()).thenReturn(method);
        Mockito.when(signature.getDeclaringTypeName()).thenReturn(FixtureService.class.getName());
        ProceedingJoinPoint joinPoint = Mockito.mock(ProceedingJoinPoint.class);
        Mockito.when(joinPoint.getSignature()).thenReturn(signature);
        if (failure == null) {
            Mockito.when(joinPoint.proceed()).thenReturn(result);
        } else {
            Mockito.when(joinPoint.proceed()).thenThrow(failure);
        }
        return joinPoint;
    }

    private SysLogEvent capturedEvent(SysLogProducer producer) {
        ArgumentCaptor<SysLogEvent> captor = ArgumentCaptor.forClass(SysLogEvent.class);
        Mockito.verify(producer).produce(captor.capture());
        return captor.getValue();
    }

    private static class FixtureService {

        @SysLog(value = "create user", type = "USER")
        public void success() {}

        @SysLog(value = "delete user", type = "USER")
        public void failure() {}
    }
}
