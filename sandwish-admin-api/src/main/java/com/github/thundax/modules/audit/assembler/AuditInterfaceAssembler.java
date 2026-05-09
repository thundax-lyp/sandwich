package com.github.thundax.modules.audit.assembler;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.audit.controller.request.AuditLogPageRequest;
import com.github.thundax.modules.audit.controller.request.AuditMetaRequest;
import com.github.thundax.modules.audit.controller.response.AuditFieldResponse;
import com.github.thundax.modules.audit.controller.response.AuditLogResponse;
import com.github.thundax.modules.audit.controller.response.AuditMetaResponse;
import com.github.thundax.modules.audit.entity.AuditLog;
import com.github.thundax.modules.audit.entity.AuditMeta;
import com.github.thundax.modules.audit.entity.enums.AuditAction;
import com.github.thundax.modules.audit.entity.enums.AuditOperatorType;
import com.github.thundax.modules.audit.entity.valueobject.AuditChangedField;
import com.github.thundax.modules.audit.service.query.AuditLogQuery;
import com.github.thundax.modules.audit.service.query.AuditMetaQuery;
import java.util.ArrayList;
import java.util.List;

public final class AuditInterfaceAssembler {

    private AuditInterfaceAssembler() {}

    public static AuditMetaQuery toMetaQuery(AuditMetaRequest request) {
        AuditMetaQuery query = new AuditMetaQuery();
        query.setObjectType(request.getObjectType());
        query.setObjectId(request.getObjectId());
        return query;
    }

    public static AuditLogQuery toLogQuery(AuditLogPageRequest request) {
        AuditLogQuery query = new AuditLogQuery();
        query.setObjectType(request.getObjectType());
        query.setObjectId(request.getObjectId());
        query.setAction(request.getAction() == null ? null : AuditAction.from(request.getAction()));
        query.setOperatorType(
                request.getOperatorType() == null ? null : AuditOperatorType.from(request.getOperatorType()));
        query.setOperatorId(request.getOperatorId());
        query.setSource(request.getSource());
        query.setRequestId(request.getRequestId());
        query.setBeginDate(request.getBeginDate());
        query.setEndDate(request.getEndDate());
        return query;
    }

    public static AuditMetaResponse toMetaResponse(AuditMeta entity) {
        AuditMetaResponse response = new AuditMetaResponse();
        if (entity == null) {
            return response;
        }
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setObjectType(entity.getObjectType());
        response.setObjectId(entity.getObjectId());
        response.setVersion(entity.getVersion());
        response.setLastAction(
                entity.getLastAction() == null ? null : entity.getLastAction().value());
        response.setLastOperatorName(entity.getLastOperatorName());
        response.setLastOperatedAt(entity.getLastOperatedAt());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }

    public static AuditLogResponse toLogResponse(AuditLog entity) {
        AuditLogResponse response = new AuditLogResponse();
        if (entity == null) {
            return response;
        }
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setObjectType(entity.getObjectType());
        response.setObjectId(entity.getObjectId());
        response.setVersion(entity.getVersion());
        response.setAction(
                entity.getAction() == null ? null : entity.getAction().value());
        response.setOperatorType(
                entity.getOperatorType() == null
                        ? null
                        : entity.getOperatorType().value());
        response.setOperatorName(entity.getOperatorName());
        response.setSource(entity.getSource());
        response.setSummary(entity.getSummary());
        response.setOccurredAt(entity.getOccurredAt());
        response.setChangedFields(toFieldResponses(entity.getChangedFields()));
        return response;
    }

    private static List<AuditFieldResponse> toFieldResponses(List<AuditChangedField> fields) {
        List<AuditFieldResponse> responses = new ArrayList<>();
        if (fields == null) {
            return responses;
        }
        for (AuditChangedField field : fields) {
            AuditFieldResponse response = new AuditFieldResponse();
            response.setFieldName(field.getFieldName());
            response.setFieldLabel(field.getFieldLabel());
            response.setBeforeDisplayValue(field.getBeforeDisplayValue());
            response.setAfterDisplayValue(field.getAfterDisplayValue());
            responses.add(response);
        }
        return responses;
    }
}
