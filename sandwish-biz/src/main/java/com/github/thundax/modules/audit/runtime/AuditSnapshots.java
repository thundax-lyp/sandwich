package com.github.thundax.modules.audit.runtime;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.audit.entity.valueobject.AuditField;
import com.github.thundax.modules.audit.entity.valueobject.AuditSnapshot;
import java.util.Arrays;

public final class AuditSnapshots {

    private AuditSnapshots() {}

    public static AuditSnapshot of(String objectType, Object id, String displayName, AuditField... fields) {
        AuditSnapshot snapshot = new AuditSnapshot();
        snapshot.setObjectType(objectType);
        snapshot.setObjectId(toObjectId(id));
        snapshot.setDisplayName(displayName);
        snapshot.setFields(Arrays.asList(fields));
        return snapshot;
    }

    private static String toObjectId(Object id) {
        if (id == null) {
            return null;
        }
        return id instanceof EntityId ? EntityIdCodec.toStringValue((EntityId) id) : String.valueOf(id);
    }

    public static AuditField field(String name, String label, Object value) {
        return new AuditField(name, label, value, value == null ? null : String.valueOf(value), "STRING", false);
    }
}
