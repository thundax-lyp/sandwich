package com.github.thundax.modules.exception;

import com.github.thundax.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class BizExceptionBoundaryAspect {

    public static final String TECHNICAL_FAILURE_CODE = "BIZ-00001";
    public static final String TECHNICAL_FAILURE_MESSAGE_KEY = "biz.exception.technical-failure";
    public static final String TECHNICAL_FAILURE_MESSAGE = "业务处理失败";

    @Around("(@within(com.github.thundax.modules.exception.BizExceptionBoundary)"
            + " && !@annotation(com.github.thundax.modules.exception.BizExceptionBoundaryIgnore))"
            + " || @annotation(com.github.thundax.modules.exception.BizExceptionBoundary)")
    public Object convertTechnicalException(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (BizException exception) {
            throw exception;
        } catch (Error error) {
            throw error;
        } catch (Throwable throwable) {
            log.warn("technical exception converted to BizException", throwable);
            throw new BizException(
                    TECHNICAL_FAILURE_CODE, TECHNICAL_FAILURE_MESSAGE_KEY, TECHNICAL_FAILURE_MESSAGE, throwable);
        }
    }
}
