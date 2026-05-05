package com.github.thundax.common.log.aspect;

import com.github.thundax.common.log.annotation.SysLog;
import com.github.thundax.common.log.model.SysLogEvent;
import com.github.thundax.common.log.producer.SysLogProducer;
import java.util.Date;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public class SysLogAspect {

    private final SysLogProducer sysLogProducer;

    public SysLogAspect(SysLogProducer sysLogProducer) {
        this.sysLogProducer = sysLogProducer;
    }

    @Around("@annotation(sysLog)")
    public Object around(ProceedingJoinPoint joinPoint, SysLog sysLog) throws Throwable {
        long startedAt = System.currentTimeMillis();
        SysLogEvent event = createEvent(joinPoint, sysLog);
        try {
            Object result = joinPoint.proceed();
            event.setSuccess(true);
            return result;
        } catch (Throwable ex) {
            event.setSuccess(false);
            event.setErrorMessage(ex.getMessage());
            throw ex;
        } finally {
            event.setCostMillis(System.currentTimeMillis() - startedAt);
            sysLogProducer.produce(event);
        }
    }

    private SysLogEvent createEvent(ProceedingJoinPoint joinPoint, SysLog sysLog) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        SysLogEvent event = new SysLogEvent();
        event.setAction(sysLog.value());
        event.setType(sysLog.type());
        event.setClassName(signature.getDeclaringTypeName());
        event.setMethodName(signature.getMethod().getName());
        event.setOccurTime(new Date());
        return event;
    }
}
