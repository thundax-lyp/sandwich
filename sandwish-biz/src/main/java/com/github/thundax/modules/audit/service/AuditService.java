package com.github.thundax.modules.audit.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.modules.audit.entity.AuditLog;
import com.github.thundax.modules.audit.entity.AuditMeta;
import com.github.thundax.modules.audit.service.command.CreateAuditLogCommand;
import com.github.thundax.modules.audit.service.query.AuditLogQuery;
import com.github.thundax.modules.audit.service.query.AuditMetaQuery;
import java.util.List;

public interface AuditService {

    EntityId record(CreateAuditLogCommand command);

    AuditLog getLog(AuditLogQuery query);

    AuditMeta getMeta(AuditMetaQuery query);

    List<AuditLog> list(AuditMetaQuery query);

    PageResult<AuditLog> page(AuditLogQuery query, PageQuery pageQuery);
}
