package com.github.thundax.modules.audit.entity;

import com.github.thundax.modules.audit.entity.enums.AuditAction;
import com.github.thundax.modules.audit.entity.enums.AuditOperatorType;
import com.github.thundax.modules.audit.entity.valueobject.AuditChangedField;
import com.github.thundax.modules.audit.entity.valueobject.AuditLogId;
import com.github.thundax.modules.audit.entity.valueobject.AuditMetaId;
import com.github.thundax.modules.audit.entity.valueobject.AuditSnapshot;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditLog {

    private AuditLogId id;
    private AuditMetaId metaId;
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
