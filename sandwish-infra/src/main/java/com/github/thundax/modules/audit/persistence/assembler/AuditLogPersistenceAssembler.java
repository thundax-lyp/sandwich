package com.github.thundax.modules.audit.persistence.assembler;

import com.alibaba.fastjson.JSON;
import com.github.thundax.modules.audit.entity.AuditLog;
import com.github.thundax.modules.audit.entity.enums.AuditAction;
import com.github.thundax.modules.audit.entity.enums.AuditOperatorType;
import com.github.thundax.modules.audit.entity.valueobject.AuditChangedField;
import com.github.thundax.modules.audit.entity.valueobject.AuditLogIdCodec;
import com.github.thundax.modules.audit.entity.valueobject.AuditMetaIdCodec;
import com.github.thundax.modules.audit.entity.valueobject.AuditSnapshot;
import com.github.thundax.modules.audit.persistence.dataobject.AuditLogDO;

import java.util.ArrayList;
import java.util.List;

public final class AuditLogPersistenceAssembler {

    private AuditLogPersistenceAssembler() {}

    public static AuditLogDO toDataObject(AuditLog entity) {
        if (entity == null) {
            return null;
        }
        AuditLogDO dataObject = new AuditLogDO();
        dataObject.setId(AuditLogIdCodec.toValue(entity.getId()));
        dataObject.setMetaId(AuditMetaIdCodec.toValue(entity.getMetaId()));
        dataObject.setObjectType(entity.getObjectType());
        dataObject.setObjectId(entity.getObjectId());
        dataObject.setVersion(entity.getVersion());
        dataObject.setPreviousVersion(entity.getPreviousVersion());
        dataObject.setAction(
                entity.getAction() == null ? null : entity.getAction().value());
        dataObject.setIdempotencyKey(entity.getIdempotencyKey());
        dataObject.setOperatorType(
                entity.getOperatorType() == null
                        ? null
                        : entity.getOperatorType().value());
        dataObject.setOperatorId(entity.getOperatorId());
        dataObject.setOperatorName(entity.getOperatorName());
        dataObject.setSource(entity.getSource());
        dataObject.setRequestId(entity.getRequestId());
        dataObject.setTraceId(entity.getTraceId());
        dataObject.setRemoteAddr(entity.getRemoteAddr());
        dataObject.setSummary(entity.getSummary());
        dataObject.setSnapshotSchemaVersion(entity.getSnapshotSchemaVersion());
        dataObject.setBeforeSnapshot(JSON.toJSONString(entity.getBeforeSnapshot()));
        dataObject.setAfterSnapshot(JSON.toJSONString(entity.getAfterSnapshot()));
        dataObject.setChangedFields(JSON.toJSONString(entity.getChangedFields()));
        dataObject.setOccurredAt(entity.getOccurredAt());
        return dataObject;
    }

    public static AuditLog toEntity(AuditLogDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        AuditLog entity = new AuditLog();
        entity.setId(AuditLogIdCodec.toDomain(dataObject.getId()));
        entity.setMetaId(AuditMetaIdCodec.toDomain(dataObject.getMetaId()));
        entity.setObjectType(dataObject.getObjectType());
        entity.setObjectId(dataObject.getObjectId());
        entity.setVersion(dataObject.getVersion());
        entity.setPreviousVersion(dataObject.getPreviousVersion());
        entity.setAction(AuditAction.from(dataObject.getAction()));
        entity.setIdempotencyKey(dataObject.getIdempotencyKey());
        entity.setOperatorType(AuditOperatorType.from(dataObject.getOperatorType()));
        entity.setOperatorId(dataObject.getOperatorId());
        entity.setOperatorName(dataObject.getOperatorName());
        entity.setSource(dataObject.getSource());
        entity.setRequestId(dataObject.getRequestId());
        entity.setTraceId(dataObject.getTraceId());
        entity.setRemoteAddr(dataObject.getRemoteAddr());
        entity.setSummary(dataObject.getSummary());
        entity.setSnapshotSchemaVersion(dataObject.getSnapshotSchemaVersion());
        entity.setBeforeSnapshot(JSON.parseObject(dataObject.getBeforeSnapshot(), AuditSnapshot.class));
        entity.setAfterSnapshot(JSON.parseObject(dataObject.getAfterSnapshot(), AuditSnapshot.class));
        entity.setChangedFields(JSON.parseArray(dataObject.getChangedFields(), AuditChangedField.class));
        entity.setOccurredAt(dataObject.getOccurredAt());
        return entity;
    }

    public static List<AuditLog> toEntityList(List<AuditLogDO> dataObjects) {
        List<AuditLog> entities = new ArrayList<>();
        if (dataObjects == null) {
            return entities;
        }
        for (AuditLogDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }
}
