package com.github.thundax.modules.audit.runtime;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.thundax.modules.audit.annotation.AuditLog;
import com.github.thundax.modules.audit.entity.enums.AuditAction;
import com.github.thundax.modules.audit.entity.valueobject.AuditLogId;
import com.github.thundax.modules.audit.service.AuditService;
import com.github.thundax.modules.audit.service.command.CreateAuditLogCommand;
import java.lang.reflect.Method;
import java.util.Collections;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Test;

public class AuditLogAspectTest {

    @Test
    public void shouldRecordCreateResultId() throws Throwable {
        RecordingAuditService auditService = new RecordingAuditService();
        AuditLogAspect aspect = new AuditLogAspect(
                auditService,
                new AuditExpressionEvaluator(),
                new AuditObjectLoaderRegistry(Collections.emptyList()),
                new AuditSnapshotAssemblerRegistry(Collections.emptyList()),
                new AuditOperatorResolver());
        Method method = Target.class.getMethod("create");
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[0]);
        when(joinPoint.proceed()).thenReturn(AuditLogId.of(1001L));

        Object result = aspect.around(joinPoint, method.getAnnotation(AuditLog.class));

        assertEquals(AuditLogId.of(1001L), result);
        assertEquals("User", auditService.command.getObjectType());
        assertEquals("1001", auditService.command.getObjectId());
        assertEquals(AuditAction.CREATE, auditService.command.getAction());
    }

    private static class RecordingAuditService implements AuditService {

        private CreateAuditLogCommand command;

        @Override
        public AuditLogId record(CreateAuditLogCommand command) {
            this.command = command;
            return AuditLogId.of(9001L);
        }

        @Override
        public com.github.thundax.modules.audit.entity.AuditLog getLog(
                com.github.thundax.modules.audit.entity.valueobject.AuditLogId id) {
            return null;
        }

        @Override
        public com.github.thundax.modules.audit.entity.AuditMeta getMeta(
                com.github.thundax.modules.audit.service.query.AuditMetaQuery query) {
            return null;
        }

        @Override
        public java.util.List<com.github.thundax.modules.audit.entity.AuditLog> list(
                com.github.thundax.modules.audit.service.query.AuditMetaQuery query) {
            return Collections.emptyList();
        }

        @Override
        public com.github.thundax.common.page.PageResult<com.github.thundax.modules.audit.entity.AuditLog> page(
                com.github.thundax.modules.audit.service.query.AuditLogQuery query,
                com.github.thundax.common.page.PageQuery pageQuery) {
            return new com.github.thundax.common.page.PageResult<>();
        }
    }

    private static class Target {

        @AuditLog(type = "User", id = "", action = AuditAction.CREATE, summary = "创建用户")
        public AuditLogId create() {
            return AuditLogId.of(1001L);
        }
    }
}
