package com.github.thundax.modules.audit.dao;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.audit.entity.AuditMeta;
import com.github.thundax.modules.audit.entity.valueobject.AuditObjectRef;

public interface AuditMetaDao {

    AuditMeta getByObjectRef(AuditObjectRef objectRef);

    EntityId insert(AuditMeta meta);

    int update(AuditMeta meta);
}
