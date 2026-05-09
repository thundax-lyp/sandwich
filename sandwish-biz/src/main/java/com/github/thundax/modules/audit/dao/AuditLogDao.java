package com.github.thundax.modules.audit.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.audit.entity.AuditLog;
import com.github.thundax.modules.audit.entity.enums.AuditAction;
import com.github.thundax.modules.audit.entity.enums.AuditOperatorType;
import java.util.Date;
import java.util.List;

public interface AuditLogDao {

    EntityId insert(AuditLog log);

    AuditLog getById(EntityId id);

    AuditLog getByIdempotencyKey(String idempotencyKey);

    List<AuditLog> listByObject(String objectType, String objectId);

    Page<AuditLog> page(
            String objectType,
            String objectId,
            AuditAction action,
            AuditOperatorType operatorType,
            String operatorId,
            String source,
            String requestId,
            Date beginDate,
            Date endDate,
            int pageNo,
            int pageSize);
}
