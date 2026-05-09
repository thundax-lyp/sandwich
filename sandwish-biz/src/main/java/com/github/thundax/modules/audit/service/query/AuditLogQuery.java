package com.github.thundax.modules.audit.service.query;

import com.github.thundax.modules.audit.entity.enums.AuditAction;
import com.github.thundax.modules.audit.entity.enums.AuditOperatorType;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditLogQuery {

    private String objectType;
    private String objectId;
    private AuditAction action;
    private AuditOperatorType operatorType;
    private String operatorId;
    private String source;
    private String requestId;
    private Date beginDate;
    private Date endDate;
}
