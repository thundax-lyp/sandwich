package com.github.thundax.modules.audit.assembler;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.modules.audit.controller.request.AuditLogDetailRequest;
import com.github.thundax.modules.audit.controller.request.AuditLogPageRequest;
import com.github.thundax.modules.audit.controller.request.AuditMetaRequest;
import com.github.thundax.modules.audit.controller.request.AuditObjectPageRequest;
import com.github.thundax.modules.audit.controller.response.AuditFieldResponse;
import com.github.thundax.modules.audit.controller.response.AuditLogDetailResponse;
import com.github.thundax.modules.audit.controller.response.AuditLogResponse;
import com.github.thundax.modules.audit.controller.response.AuditMetaResponse;
import com.github.thundax.modules.audit.controller.response.AuditObjectFieldResponse;
import com.github.thundax.modules.audit.controller.response.AuditObjectOverviewResponse;
import com.github.thundax.modules.audit.controller.response.AuditOptionResponse;
import com.github.thundax.modules.audit.controller.response.AuditOptionsResponse;
import com.github.thundax.modules.audit.controller.response.AuditSnapshotFieldResponse;
import com.github.thundax.modules.audit.controller.response.AuditSnapshotResponse;
import com.github.thundax.modules.audit.entity.AuditLog;
import com.github.thundax.modules.audit.entity.AuditMeta;
import com.github.thundax.modules.audit.entity.enums.AuditAction;
import com.github.thundax.modules.audit.entity.enums.AuditOperatorType;
import com.github.thundax.modules.audit.entity.valueobject.AuditChangedField;
import com.github.thundax.modules.audit.entity.valueobject.AuditField;
import com.github.thundax.modules.audit.entity.valueobject.AuditSnapshot;
import com.github.thundax.modules.audit.service.query.AuditLogQuery;
import com.github.thundax.modules.audit.service.query.AuditMetaQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class AuditInterfaceAssembler {

    private static final Map<String, String> OBJECT_TYPE_LABELS = new LinkedHashMap<>();
    private static final Map<String, List<AuditObjectFieldResponse>> OBJECT_FIELDS = new LinkedHashMap<>();

    static {
        OBJECT_TYPE_LABELS.put("User", "后台用户");
        OBJECT_TYPE_LABELS.put("Role", "角色");
        OBJECT_TYPE_LABELS.put("Menu", "菜单");
        OBJECT_TYPE_LABELS.put("Department", "部门");
        OBJECT_TYPE_LABELS.put("Dict", "字典");
        OBJECT_TYPE_LABELS.put("Member", "会员");
        OBJECT_TYPE_LABELS.put("AsyncTask", "异步任务");

        OBJECT_FIELDS.put("User", fields(field("name", "名称"), field("status", "状态"), field("privilege", "权限")));
        OBJECT_FIELDS.put("Role", fields(field("name", "名称"), field("status", "状态"), field("privilege", "权限")));
        OBJECT_FIELDS.put("Menu", fields(field("name", "名称"), field("perms", "权限"), field("visibility", "可见性")));
        OBJECT_FIELDS.put("Department", fields(field("name", "名称"), field("shortName", "简称"), field("parentId", "父级")));
        OBJECT_FIELDS.put("Dict", fields(field("type", "类型"), field("label", "标签"), field("value", "值")));
        OBJECT_FIELDS.put("Member", fields(field("name", "名称"), field("status", "状态"), field("gender", "性别")));
        OBJECT_FIELDS.put("AsyncTask", fields(field("title", "标题"), field("status", "状态"), field("message", "消息")));
    }

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

    public static AuditLogQuery toLogQuery(AuditLogDetailRequest request) {
        AuditLogQuery query = new AuditLogQuery();
        query.setId(EntityIdCodec.toDomain(request.getId()));
        return query;
    }

    public static AuditLogQuery toLogQuery(AuditObjectPageRequest request) {
        AuditLogQuery query = new AuditLogQuery();
        query.setObjectType(request.getObjectType());
        query.setObjectId(request.getObjectId());
        return query;
    }

    public static AuditLogQuery toObjectLogQuery(AuditMetaRequest request) {
        AuditLogQuery query = new AuditLogQuery();
        query.setObjectType(request.getObjectType());
        query.setObjectId(request.getObjectId());
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
        fillLogResponse(response, entity);
        return response;
    }

    public static AuditLogDetailResponse toLogDetailResponse(AuditLog entity) {
        AuditLogDetailResponse response = new AuditLogDetailResponse();
        fillLogResponse(response, entity);
        if (entity == null) {
            return response;
        }
        response.setIdempotencyKey(entity.getIdempotencyKey());
        response.setPreviousVersion(entity.getPreviousVersion());
        response.setBeforeSnapshot(toSnapshotResponse(entity.getBeforeSnapshot()));
        response.setAfterSnapshot(toSnapshotResponse(entity.getAfterSnapshot()));
        return response;
    }

    public static AuditObjectOverviewResponse toOverviewResponse(AuditMeta meta, PageResult<AuditLog> latestLogs) {
        AuditObjectOverviewResponse response = new AuditObjectOverviewResponse();
        response.setMeta(toMetaResponse(meta));
        if (latestLogs != null && latestLogs.getRecords() != null) {
            response.setLatestLogs(latestLogs.getRecords().stream()
                    .map(AuditInterfaceAssembler::toLogResponse)
                    .collect(Collectors.toList()));
        }
        return response;
    }

    public static AuditOptionsResponse toOptionsResponse() {
        AuditOptionsResponse response = new AuditOptionsResponse();
        response.setObjectTypes(OBJECT_TYPE_LABELS.entrySet().stream()
                .map(entry -> option(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList()));
        response.setActions(Arrays.stream(AuditAction.values())
                .map(action -> option(action.value(), actionLabel(action)))
                .collect(Collectors.toList()));
        response.setOperatorTypes(Arrays.stream(AuditOperatorType.values())
                .map(type -> option(type.value(), operatorTypeLabel(type)))
                .collect(Collectors.toList()));
        return response;
    }

    public static List<AuditObjectFieldResponse> toFieldResponses(String objectType) {
        List<AuditObjectFieldResponse> fields = OBJECT_FIELDS.get(objectType);
        if (fields == null) {
            return new ArrayList<>();
        }
        return fields.stream()
                .map(field -> field(field.getFieldName(), field.getFieldLabel()))
                .collect(Collectors.toList());
    }

    private static void fillLogResponse(AuditLogResponse response, AuditLog entity) {
        if (entity == null) {
            return;
        }
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setObjectType(entity.getObjectType());
        response.setObjectTypeLabel(objectTypeLabel(entity.getObjectType()));
        response.setObjectId(entity.getObjectId());
        response.setObjectDisplayName(displayName(entity));
        response.setVersion(entity.getVersion());
        response.setAction(
                entity.getAction() == null ? null : entity.getAction().value());
        response.setActionLabel(actionLabel(entity.getAction()));
        response.setOperatorType(
                entity.getOperatorType() == null
                        ? null
                        : entity.getOperatorType().value());
        response.setOperatorTypeLabel(operatorTypeLabel(entity.getOperatorType()));
        response.setOperatorId(entity.getOperatorId());
        response.setOperatorName(entity.getOperatorName());
        response.setSource(entity.getSource());
        response.setRequestId(entity.getRequestId());
        response.setTraceId(entity.getTraceId());
        response.setRemoteAddr(entity.getRemoteAddr());
        response.setSummary(entity.getSummary());
        response.setOccurredAt(entity.getOccurredAt());
        response.setChangedFields(toChangedFieldResponses(entity.getChangedFields()));
        response.setChangedFieldCount(
                entity.getChangedFields() == null
                        ? 0
                        : entity.getChangedFields().size());
    }

    private static AuditSnapshotResponse toSnapshotResponse(AuditSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        AuditSnapshotResponse response = new AuditSnapshotResponse();
        response.setObjectType(snapshot.getObjectType());
        response.setObjectId(snapshot.getObjectId());
        response.setDisplayName(snapshot.getDisplayName());
        response.setFields(toSnapshotFieldResponses(snapshot.getFields()));
        return response;
    }

    private static List<AuditSnapshotFieldResponse> toSnapshotFieldResponses(List<AuditField> fields) {
        List<AuditSnapshotFieldResponse> responses = new ArrayList<>();
        if (fields == null) {
            return responses;
        }
        for (AuditField field : fields) {
            AuditSnapshotFieldResponse response = new AuditSnapshotFieldResponse();
            response.setFieldName(field.getFieldName());
            response.setFieldLabel(field.getFieldLabel());
            response.setValue(field.getValue());
            response.setDisplayValue(field.getDisplayValue());
            response.setValueType(field.getValueType());
            response.setSensitive(field.isSensitive());
            responses.add(response);
        }
        return responses;
    }

    private static List<AuditFieldResponse> toChangedFieldResponses(List<AuditChangedField> fields) {
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

    private static String displayName(AuditLog entity) {
        if (entity.getAfterSnapshot() != null && entity.getAfterSnapshot().getDisplayName() != null) {
            return entity.getAfterSnapshot().getDisplayName();
        }
        if (entity.getBeforeSnapshot() != null) {
            return entity.getBeforeSnapshot().getDisplayName();
        }
        return null;
    }

    private static String objectTypeLabel(String objectType) {
        return OBJECT_TYPE_LABELS.get(objectType);
    }

    private static String actionLabel(AuditAction action) {
        if (action == null) {
            return null;
        }
        switch (action) {
            case CREATE:
                return "创建";
            case UPDATE:
                return "更新";
            case DELETE:
                return "删除";
            case ENABLE:
                return "启用";
            case DISABLE:
                return "禁用";
            case ARCHIVE:
                return "归档";
            case RESTORE:
                return "恢复";
            case BIND:
                return "绑定";
            case UNBIND:
                return "解绑";
            case UPDATE_RELATION:
                return "更新关系";
            case RESET_CREDENTIAL:
                return "重置凭据";
            default:
                return action.value();
        }
    }

    private static String operatorTypeLabel(AuditOperatorType operatorType) {
        if (operatorType == null) {
            return null;
        }
        switch (operatorType) {
            case USER:
                return "后台用户";
            case MEMBER:
                return "会员";
            case SYSTEM:
                return "系统";
            case UNKNOWN:
                return "未知";
            default:
                return operatorType.value();
        }
    }

    private static AuditOptionResponse option(String value, String label) {
        AuditOptionResponse response = new AuditOptionResponse();
        response.setValue(value);
        response.setLabel(label);
        return response;
    }

    private static AuditObjectFieldResponse field(String fieldName, String fieldLabel) {
        AuditObjectFieldResponse response = new AuditObjectFieldResponse();
        response.setFieldName(fieldName);
        response.setFieldLabel(fieldLabel);
        return response;
    }

    private static List<AuditObjectFieldResponse> fields(AuditObjectFieldResponse... fields) {
        return Arrays.asList(fields);
    }
}
