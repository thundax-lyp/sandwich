package com.github.thundax.modules.audit.persistence.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.github.thundax.modules.audit.entity.AuditLog;
import com.github.thundax.modules.audit.entity.enums.AuditAction;
import com.github.thundax.modules.audit.entity.enums.AuditOperatorType;
import com.github.thundax.modules.audit.entity.valueobject.AuditChangedField;
import com.github.thundax.modules.audit.entity.valueobject.AuditField;
import com.github.thundax.modules.audit.entity.valueobject.AuditLogId;
import com.github.thundax.modules.audit.entity.valueobject.AuditMetaId;
import com.github.thundax.modules.audit.entity.valueobject.AuditSnapshot;
import com.github.thundax.modules.audit.persistence.dataobject.AuditLogDO;
import java.util.Arrays;
import org.junit.Test;

public class AuditLogPersistenceAssemblerTest {

    @Test
    public void shouldPersistAuditJsonFieldsWithJackson() {
        AuditLog entity = new AuditLog();
        entity.setId(AuditLogId.of(1001L));
        entity.setMetaId(AuditMetaId.of(2001L));
        entity.setObjectType("SUBMISSION");
        entity.setObjectId("3001");
        entity.setVersion(2L);
        entity.setPreviousVersion(1L);
        entity.setAction(AuditAction.UPDATE);
        entity.setOperatorType(AuditOperatorType.USER);
        entity.setBeforeSnapshot(snapshot("before-title"));
        entity.setAfterSnapshot(snapshot("after-title"));
        entity.setChangedFields(Arrays.asList(
                new AuditChangedField("title", "标题", "before-title", "before-title", "after-title", "after-title")));

        AuditLogDO dataObject = AuditLogPersistenceAssembler.toDataObject(entity);

        assertEquals("{", dataObject.getBeforeSnapshot().substring(0, 1));
        assertEquals("{", dataObject.getAfterSnapshot().substring(0, 1));
        assertEquals("[", dataObject.getChangedFields().substring(0, 1));

        AuditLog restored = AuditLogPersistenceAssembler.toEntity(dataObject);

        assertNotNull(restored.getBeforeSnapshot());
        assertNotNull(restored.getAfterSnapshot());
        assertEquals("before-title", restored.getBeforeSnapshot().getDisplayName());
        assertEquals("after-title", restored.getAfterSnapshot().getDisplayName());
        assertEquals(1, restored.getChangedFields().size());
        assertEquals("title", restored.getChangedFields().get(0).getFieldName());
        assertEquals("after-title", restored.getChangedFields().get(0).getAfterDisplayValue());
    }

    private AuditSnapshot snapshot(String displayName) {
        AuditSnapshot snapshot = new AuditSnapshot();
        snapshot.setObjectType("SUBMISSION");
        snapshot.setObjectId("3001");
        snapshot.setDisplayName(displayName);
        snapshot.getFields().add(new AuditField("title", "标题", displayName, displayName, "STRING", false));
        return snapshot;
    }
}
