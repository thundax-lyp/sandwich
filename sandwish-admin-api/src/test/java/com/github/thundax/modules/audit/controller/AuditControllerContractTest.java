package com.github.thundax.modules.audit.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.thundax.modules.audit.controller.request.AuditLogDetailRequest;
import com.github.thundax.modules.audit.controller.request.AuditObjectFieldRequest;
import com.github.thundax.modules.audit.controller.response.AuditLogDetailResponse;
import com.github.thundax.modules.audit.controller.response.AuditObjectFieldResponse;
import com.github.thundax.modules.audit.controller.response.AuditOptionsResponse;
import com.github.thundax.modules.audit.entity.AuditLog;
import com.github.thundax.modules.audit.entity.enums.AuditAction;
import com.github.thundax.modules.audit.entity.enums.AuditOperatorType;
import com.github.thundax.modules.audit.entity.valueobject.AuditLogId;
import com.github.thundax.modules.audit.entity.valueobject.AuditLogIdCodec;
import com.github.thundax.modules.audit.runtime.AuditSnapshots;
import com.github.thundax.modules.audit.service.AuditService;
import java.util.List;
import org.junit.Test;

public class AuditControllerContractTest {

    @Test
    public void shouldReturnAuditDetailForUi() {
        AuditService auditService = mock(AuditService.class);
        AuditController controller = new AuditController(auditService);
        AuditLog log = auditLog();
        when(auditService.getLog(org.mockito.ArgumentMatchers.argThat(queryWithId(1001L))))
                .thenReturn(log);

        AuditLogDetailResponse response = controller.detail(detailRequest(1001L));

        assertEquals(Long.valueOf(1001L), response.getId());
        assertEquals("User", response.getObjectType());
        assertEquals("后台用户", response.getObjectTypeLabel());
        assertEquals("创建", response.getActionLabel());
        assertEquals("后台用户", response.getOperatorTypeLabel());
        assertEquals("request-1", response.getRequestId());
        assertEquals("trace-1", response.getTraceId());
        assertEquals("admin", response.getObjectDisplayName());
        assertNotNull(response.getAfterSnapshot());
        assertEquals(1, response.getAfterSnapshot().getFields().size());
    }

    @Test
    public void shouldReturnAuditOptionsAndObjectFieldsForUi() {
        AuditController controller = new AuditController(mock(AuditService.class));

        AuditOptionsResponse options = controller.options();
        List<AuditObjectFieldResponse> fields = controller.fields(fieldRequest("User"));

        assertFalse(options.getObjectTypes().isEmpty());
        assertFalse(options.getActions().isEmpty());
        assertFalse(options.getOperatorTypes().isEmpty());
        assertEquals("User", options.getObjectTypes().get(0).getValue());
        assertEquals("CREATE", options.getActions().get(0).getValue());
        assertEquals("name", fields.get(0).getFieldName());
        assertEquals("名称", fields.get(0).getFieldLabel());
    }

    private AuditLogDetailRequest detailRequest(Long id) {
        AuditLogDetailRequest request = new AuditLogDetailRequest();
        request.setId(id);
        return request;
    }

    private AuditObjectFieldRequest fieldRequest(String objectType) {
        AuditObjectFieldRequest request = new AuditObjectFieldRequest();
        request.setObjectType(objectType);
        return request;
    }

    private AuditLog auditLog() {
        AuditLog log = new AuditLog();
        log.setId(AuditLogIdCodec.toDomain(1001L));
        log.setObjectType("User");
        log.setObjectId("2001");
        log.setVersion(1L);
        log.setAction(AuditAction.CREATE);
        log.setOperatorType(AuditOperatorType.USER);
        log.setOperatorId("3001");
        log.setOperatorName("operator");
        log.setRequestId("request-1");
        log.setTraceId("trace-1");
        log.setAfterSnapshot(AuditSnapshots.of("User", "2001", "admin", AuditSnapshots.field("name", "名称", "admin")));
        return log;
    }

    private org.mockito.ArgumentMatcher<AuditLogId> queryWithId(Long id) {
        return query -> query != null && AuditLogIdCodec.toDomain(id).equals(query);
    }
}
