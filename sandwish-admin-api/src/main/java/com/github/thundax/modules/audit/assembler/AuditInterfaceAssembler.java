package com.github.thundax.modules.audit.assembler;

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
import com.github.thundax.modules.audit.entity.valueobject.AuditLogId;
import com.github.thundax.modules.audit.entity.valueobject.AuditLogIdCodec;
import com.github.thundax.modules.audit.entity.valueobject.AuditMetaIdCodec;
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
        OBJECT_TYPE_LABELS.put("Submission", "提交内容");

        OBJECT_FIELDS.put("User", fields(field("name", "名称"), field("status", "状态"), field("privilege", "权限")));
        OBJECT_FIELDS.put("Role", fields(field("name", "名称"), field("status", "状态"), field("privilege", "权限")));
        OBJECT_FIELDS.put("Menu", fields(field("name", "名称"), field("perms", "权限"), field("visibility", "可见性")));
        OBJECT_FIELDS.put("Department", fields(field("name", "名称"), field("shortName", "简称"), field("parentId", "父级")));
        OBJECT_FIELDS.put("Dict", fields(field("type", "类型"), field("label", "标签"), field("value", "值")));
        OBJECT_FIELDS.put("Member", fields(field("name", "名称"), field("status", "状态"), field("gender", "性别")));
        OBJECT_FIELDS.put("Submission", fields(field("title", "标题"), field("content", "正文"), field("status", "状态")));
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

    public static AuditLogId toLogId(AuditLogDetailRequest request) {
        return AuditLogIdCodec.toDomain(request.getId());
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
        if (entity == null) {
            return AuditMetaResponse.builder().build();
        }
        return AuditMetaResponse.builder()
                .id(AuditMetaIdCodec.toStringValue(entity.getId()))
                .objectType(entity.getObjectType())
                .objectId(entity.getObjectId())
                .version(entity.getVersion())
                .lastAction(
                        entity.getLastAction() == null
                                ? null
                                : entity.getLastAction().value())
                .lastOperatorName(entity.getLastOperatorName())
                .lastOperatedAt(entity.getLastOperatedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static AuditLogResponse toLogResponse(AuditLog entity) {
        if (entity == null) {
            return AuditLogResponse.builder().changedFields(new ArrayList<>()).build();
        }
        return logResponseBuilder(entity).build();
    }

    public static AuditLogDetailResponse toLogDetailResponse(AuditLog entity) {
        if (entity == null) {
            return AuditLogDetailResponse.builder()
                    .changedFields(new ArrayList<>())
                    .build();
        }
        return AuditLogDetailResponse.builder()
                .id(AuditLogIdCodec.toStringValue(entity.getId()))
                .objectType(entity.getObjectType())
                .objectTypeLabel(objectTypeLabel(entity.getObjectType()))
                .objectId(entity.getObjectId())
                .objectDisplayName(displayName(entity))
                .version(entity.getVersion())
                .action(entity.getAction() == null ? null : entity.getAction().value())
                .actionLabel(actionLabel(entity.getAction()))
                .operatorType(
                        entity.getOperatorType() == null
                                ? null
                                : entity.getOperatorType().value())
                .operatorTypeLabel(operatorTypeLabel(entity.getOperatorType()))
                .operatorId(entity.getOperatorId())
                .operatorName(entity.getOperatorName())
                .source(entity.getSource())
                .requestId(entity.getRequestId())
                .traceId(entity.getTraceId())
                .remoteAddr(entity.getRemoteAddr())
                .summary(entity.getSummary())
                .occurredAt(entity.getOccurredAt())
                .changedFields(toChangedFieldResponses(entity.getChangedFields()))
                .changedFieldCount(
                        entity.getChangedFields() == null
                                ? 0
                                : entity.getChangedFields().size())
                .idempotencyKey(entity.getIdempotencyKey())
                .previousVersion(entity.getPreviousVersion())
                .beforeSnapshot(toSnapshotResponse(entity.getBeforeSnapshot()))
                .afterSnapshot(toSnapshotResponse(entity.getAfterSnapshot()))
                .build();
    }

    public static AuditObjectOverviewResponse toOverviewResponse(AuditMeta meta, PageResult<AuditLog> latestLogs) {
        return AuditObjectOverviewResponse.builder()
                .meta(toMetaResponse(meta))
                .latestLogs(
                        latestLogs == null || latestLogs.getRecords() == null
                                ? null
                                : latestLogs.getRecords().stream()
                                        .map(AuditInterfaceAssembler::toLogResponse)
                                        .collect(Collectors.toList()))
                .build();
    }

    public static AuditOptionsResponse toOptionsResponse() {
        return AuditOptionsResponse.builder()
                .objectTypes(OBJECT_TYPE_LABELS.entrySet().stream()
                        .map(entry -> option(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList()))
                .actions(Arrays.stream(AuditAction.values())
                        .map(action -> option(action.value(), actionLabel(action)))
                        .collect(Collectors.toList()))
                .operatorTypes(Arrays.stream(AuditOperatorType.values())
                        .map(type -> option(type.value(), operatorTypeLabel(type)))
                        .collect(Collectors.toList()))
                .build();
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

    private static AuditLogResponse.AuditLogResponseBuilder logResponseBuilder(AuditLog entity) {
        return AuditLogResponse.builder()
                .id(AuditLogIdCodec.toStringValue(entity.getId()))
                .objectType(entity.getObjectType())
                .objectTypeLabel(objectTypeLabel(entity.getObjectType()))
                .objectId(entity.getObjectId())
                .objectDisplayName(displayName(entity))
                .version(entity.getVersion())
                .action(entity.getAction() == null ? null : entity.getAction().value())
                .actionLabel(actionLabel(entity.getAction()))
                .operatorType(
                        entity.getOperatorType() == null
                                ? null
                                : entity.getOperatorType().value())
                .operatorTypeLabel(operatorTypeLabel(entity.getOperatorType()))
                .operatorId(entity.getOperatorId())
                .operatorName(entity.getOperatorName())
                .source(entity.getSource())
                .requestId(entity.getRequestId())
                .traceId(entity.getTraceId())
                .remoteAddr(entity.getRemoteAddr())
                .summary(entity.getSummary())
                .occurredAt(entity.getOccurredAt())
                .changedFields(toChangedFieldResponses(entity.getChangedFields()))
                .changedFieldCount(
                        entity.getChangedFields() == null
                                ? 0
                                : entity.getChangedFields().size());
    }

    private static AuditSnapshotResponse toSnapshotResponse(AuditSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        return AuditSnapshotResponse.builder()
                .objectType(snapshot.getObjectType())
                .objectId(snapshot.getObjectId())
                .displayName(snapshot.getDisplayName())
                .fields(toSnapshotFieldResponses(snapshot.getFields()))
                .build();
    }

    private static List<AuditSnapshotFieldResponse> toSnapshotFieldResponses(List<AuditField> fields) {
        List<AuditSnapshotFieldResponse> responses = new ArrayList<>();
        if (fields == null) {
            return responses;
        }
        for (AuditField field : fields) {
            responses.add(AuditSnapshotFieldResponse.builder()
                    .fieldName(field.getFieldName())
                    .fieldLabel(field.getFieldLabel())
                    .value(field.getValue())
                    .displayValue(field.getDisplayValue())
                    .valueType(field.getValueType())
                    .sensitive(field.isSensitive())
                    .build());
        }
        return responses;
    }

    private static List<AuditFieldResponse> toChangedFieldResponses(List<AuditChangedField> fields) {
        List<AuditFieldResponse> responses = new ArrayList<>();
        if (fields == null) {
            return responses;
        }
        for (AuditChangedField field : fields) {
            responses.add(AuditFieldResponse.builder()
                    .fieldName(field.getFieldName())
                    .fieldLabel(field.getFieldLabel())
                    .beforeDisplayValue(field.getBeforeDisplayValue())
                    .afterDisplayValue(field.getAfterDisplayValue())
                    .build());
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
        return AuditOptionResponse.builder().value(value).label(label).build();
    }

    private static AuditObjectFieldResponse field(String fieldName, String fieldLabel) {
        return AuditObjectFieldResponse.builder()
                .fieldName(fieldName)
                .fieldLabel(fieldLabel)
                .build();
    }

    private static List<AuditObjectFieldResponse> fields(AuditObjectFieldResponse... fields) {
        return Arrays.asList(fields);
    }
}
