package com.github.thundax.modules.audit.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.modules.audit.entity.AuditLog;
import com.github.thundax.modules.audit.service.query.AuditLogQuery;
import java.util.List;

public interface AuditLogDao {

    EntityId insert(AuditLog log);

    AuditLog getByIdempotencyKey(String idempotencyKey);

    List<AuditLog> listByObject(String objectType, String objectId);

    Page<AuditLog> page(AuditLogQuery query, PageQuery pageQuery);
}
