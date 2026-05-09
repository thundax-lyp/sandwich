package com.github.thundax.modules.audit.entity;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.audit.entity.enums.AuditAction;
import com.github.thundax.modules.audit.entity.enums.AuditOperatorType;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditMeta {

    private EntityId id;
    private String objectType;
    private String objectId;
    private Long version;
    private EntityId lastLogId;
    private AuditAction lastAction;
    private AuditOperatorType lastOperatorType;
    private String lastOperatorId;
    private String lastOperatorName;
    private Date lastOperatedAt;
    private EntityId createdLogId;
    private Date createdAt;
}
