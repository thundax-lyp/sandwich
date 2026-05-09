package com.github.thundax.modules.audit.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.github.thundax.modules.audit.entity.valueobject.AuditChangedField;
import java.util.List;
import org.junit.Test;

public class AuditDiffServiceTest {

    @Test
    public void shouldReturnChangedFieldsOnly() {
        AuditDiffService service = new AuditDiffService();

        List<AuditChangedField> changedFields = service.diff(
                AuditSnapshots.of("User", "1", "old", AuditSnapshots.field("name", "名称", "old")),
                AuditSnapshots.of("User", "1", "new", AuditSnapshots.field("name", "名称", "new")));

        assertEquals(1, changedFields.size());
        assertEquals("name", changedFields.get(0).getFieldName());
        assertEquals("old", changedFields.get(0).getBeforeValue());
        assertEquals("new", changedFields.get(0).getAfterValue());
    }

    @Test
    public void shouldReturnEmptyWhenValuesAreSame() {
        AuditDiffService service = new AuditDiffService();

        List<AuditChangedField> changedFields = service.diff(
                AuditSnapshots.of("User", "1", "same", AuditSnapshots.field("name", "名称", "same")),
                AuditSnapshots.of("User", "1", "same", AuditSnapshots.field("name", "名称", "same")));

        assertTrue(changedFields.isEmpty());
    }
}
