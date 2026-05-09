package com.github.thundax.modules.audit.entity;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.audit.entity.enums.AuditAction;
import com.github.thundax.modules.audit.entity.enums.AuditOperatorType;
import com.github.thundax.modules.audit.entity.valueobject.AuditChangedField;
import com.github.thundax.modules.audit.entity.valueobject.AuditSnapshot;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditLog {

    private EntityId id;
    private EntityId metaId;
    private String objectType;
    private String objectId;
    private Long version;
    private Long previousVersion;
    private AuditAction action;
    private String idempotencyKey;
    private AuditOperatorType operatorType;
    private String operatorId;
    private String operatorName;
    private String source;
    private String requestId;
    private String traceId;
    private String remoteAddr;
    private String summary;
    private Integer snapshotSchemaVersion = 1;
    private AuditSnapshot beforeSnapshot;
    private AuditSnapshot afterSnapshot;
    private List<AuditChangedField> changedFields = new ArrayList<>();
    private Date occurredAt;
}
