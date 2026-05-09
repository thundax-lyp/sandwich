package com.github.thundax.modules.audit.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.modules.audit.dao.AuditLogDao;
import com.github.thundax.modules.audit.dao.AuditMetaDao;
import com.github.thundax.modules.audit.entity.AuditLog;
import com.github.thundax.modules.audit.entity.AuditMeta;
import com.github.thundax.modules.audit.entity.enums.AuditAction;
import com.github.thundax.modules.audit.entity.enums.AuditOperatorType;
import com.github.thundax.modules.audit.entity.valueobject.AuditChangedField;
import com.github.thundax.modules.audit.entity.valueobject.AuditObjectRef;
import com.github.thundax.modules.audit.runtime.AuditDiffService;
import com.github.thundax.modules.audit.service.AuditService;
import com.github.thundax.modules.audit.service.command.CreateAuditLogCommand;
import com.github.thundax.modules.audit.service.query.AuditLogQuery;
import com.github.thundax.modules.audit.service.query.AuditMetaQuery;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditServiceImpl implements AuditService {

    private final AuditMetaDao auditMetaDao;
    private final AuditLogDao auditLogDao;
    private final AuditDiffService auditDiffService;

    public AuditServiceImpl(AuditMetaDao auditMetaDao, AuditLogDao auditLogDao, AuditDiffService auditDiffService) {
        this.auditMetaDao = auditMetaDao;
        this.auditLogDao = auditLogDao;
        this.auditDiffService = auditDiffService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntityId record(CreateAuditLogCommand command) {
        if (command == null
                || StringUtils.isBlank(command.getObjectType())
                || StringUtils.isBlank(command.getObjectId())) {
            return null;
        }
        if (StringUtils.isNotBlank(command.getIdempotencyKey())) {
            AuditLog existed = auditLogDao.getByIdempotencyKey(command.getIdempotencyKey());
            if (existed != null) {
                return existed.getId();
            }
        }

        List<AuditChangedField> changedFields =
                auditDiffService.diff(command.getBeforeSnapshot(), command.getAfterSnapshot());
        if (!command.isRecordWhenUnchanged() && changedFields.isEmpty()) {
            return null;
        }

        AuditObjectRef objectRef = new AuditObjectRef(command.getObjectType(), command.getObjectId());
        AuditMeta meta = auditMetaDao.getByObjectRef(objectRef);
        long previousVersion = meta == null || meta.getVersion() == null ? 0L : meta.getVersion();
        Date occurredAt = new Date();
        String idempotencyKey = StringUtils.defaultIfBlank(
                command.getIdempotencyKey(),
                command.getObjectType() + ":" + command.getObjectId() + ":" + command.getAction() + ":"
                        + occurredAt.getTime());

        AuditLog log = new AuditLog();
        log.setMetaId(meta == null ? null : meta.getId());
        log.setObjectType(command.getObjectType());
        log.setObjectId(command.getObjectId());
        log.setPreviousVersion(previousVersion);
        log.setVersion(previousVersion + 1);
        log.setAction(command.getAction() == null ? AuditAction.UPDATE : command.getAction());
        log.setIdempotencyKey(idempotencyKey);
        log.setOperatorType(command.getOperatorType() == null ? AuditOperatorType.UNKNOWN : command.getOperatorType());
        log.setOperatorId(command.getOperatorId());
        log.setOperatorName(command.getOperatorName());
        log.setSource(StringUtils.defaultIfBlank(command.getSource(), "SERVICE"));
        log.setRequestId(command.getRequestId());
        log.setTraceId(command.getTraceId());
        log.setRemoteAddr(command.getRemoteAddr());
        log.setSummary(command.getSummary());
        log.setBeforeSnapshot(command.getBeforeSnapshot());
        log.setAfterSnapshot(command.getAfterSnapshot());
        log.setChangedFields(changedFields);
        log.setOccurredAt(occurredAt);

        if (meta == null) {
            meta = new AuditMeta();
            meta.setObjectType(command.getObjectType());
            meta.setObjectId(command.getObjectId());
            meta.setVersion(1L);
            meta.setCreatedAt(occurredAt);
            EntityId metaId = auditMetaDao.insert(meta);
            meta.setId(metaId);
        }
        log.setMetaId(meta.getId());
        EntityId logId = auditLogDao.insert(log);

        meta.setLastLogId(logId);
        meta.setLastAction(log.getAction());
        meta.setLastOperatorType(log.getOperatorType());
        meta.setLastOperatorId(log.getOperatorId());
        meta.setLastOperatorName(log.getOperatorName());
        meta.setLastOperatedAt(log.getOccurredAt());
        meta.setVersion(log.getVersion());
        if (meta.getCreatedLogId() == null) {
            meta.setCreatedLogId(logId);
        }
        auditMetaDao.update(meta);
        return logId;
    }

    @Override
    public AuditMeta getMeta(AuditMetaQuery query) {
        if (query == null) {
            return null;
        }
        return auditMetaDao.getByObjectRef(new AuditObjectRef(query.getObjectType(), query.getObjectId()));
    }

    @Override
    public List<AuditLog> history(AuditMetaQuery query) {
        return auditLogDao.listByObject(query.getObjectType(), query.getObjectId());
    }

    @Override
    public Page<AuditLog> page(AuditLogQuery query, PageQuery pageQuery) {
        return auditLogDao.page(query, pageQuery);
    }
}
