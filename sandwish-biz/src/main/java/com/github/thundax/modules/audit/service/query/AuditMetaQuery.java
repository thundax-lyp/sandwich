package com.github.thundax.modules.audit.service.query;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditMetaQuery {

    private String objectType;
    private String objectId;
}
