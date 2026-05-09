package com.github.thundax.modules.audit.runtime;

import com.github.thundax.modules.audit.annotation.AuditLog;
import com.github.thundax.modules.audit.entity.valueobject.AuditSnapshot;
import com.github.thundax.modules.audit.service.AuditService;
import com.github.thundax.modules.audit.service.command.CreateAuditLogCommand;
import java.lang.reflect.Method;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditLogAspect {

    private final AuditService auditService;
    private final AuditExpressionEvaluator expressionEvaluator;
    private final AuditObjectLoaderRegistry loaderRegistry;
    private final AuditSnapshotAssemblerRegistry assemblerRegistry;
    private final AuditOperatorResolver operatorResolver;

    public AuditLogAspect(
            AuditService auditService,
            AuditExpressionEvaluator expressionEvaluator,
            AuditObjectLoaderRegistry loaderRegistry,
            AuditSnapshotAssemblerRegistry assemblerRegistry,
            AuditOperatorResolver operatorResolver) {
        this.auditService = auditService;
        this.expressionEvaluator = expressionEvaluator;
        this.loaderRegistry = loaderRegistry;
        this.assemblerRegistry = assemblerRegistry;
        this.operatorResolver = operatorResolver;
    }

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object[] args = joinPoint.getArgs();
        if (!expressionEvaluator.booleanValue(auditLog.condition(), method, args, true)) {
            return joinPoint.proceed();
        }
        String objectId = expressionEvaluator.stringValue(auditLog.id(), method, args);
        AuditSnapshot before = snapshot(auditLog.type(), objectId);
        Object result = joinPoint.proceed();
        if (objectId == null && result != null) {
            objectId = String.valueOf(result);
        }
        AuditSnapshot after = snapshot(auditLog.type(), objectId);

        CreateAuditLogCommand command = new CreateAuditLogCommand();
        command.setObjectType(auditLog.type());
        command.setObjectId(objectId);
        command.setAction(auditLog.action());
        command.setSummary(auditLog.summary());
        command.setBeforeSnapshot(before);
        command.setAfterSnapshot(after);
        command.setRecordWhenUnchanged(auditLog.recordWhenUnchanged());
        command.setOperatorType(operatorResolver.operatorType());
        command.setOperatorId(operatorResolver.operatorId());
        command.setOperatorName(operatorResolver.operatorName());
        auditService.record(command);
        return result;
    }

    private AuditSnapshot snapshot(String objectType, String objectId) {
        if (StringUtils.isBlank(objectId)) {
            return null;
        }
        AuditObjectLoader loader = loaderRegistry.get(objectType);
        AuditSnapshotAssembler assembler = assemblerRegistry.get(objectType);
        if (loader == null || assembler == null) {
            return null;
        }
        return assembler.assemble(loader.load(objectId));
    }
}
