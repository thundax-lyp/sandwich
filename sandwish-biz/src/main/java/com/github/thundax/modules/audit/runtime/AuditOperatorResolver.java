package com.github.thundax.modules.audit.runtime;

import com.github.thundax.modules.audit.entity.enums.AuditOperatorType;
import com.github.thundax.modules.auth.utils.UserAccessHolder;
import org.springframework.stereotype.Component;

@Component
public class AuditOperatorResolver {

    public AuditOperatorType operatorType() {
        return UserAccessHolder.currentUserId() == null ? AuditOperatorType.UNKNOWN : AuditOperatorType.USER;
    }

    public String operatorId() {
        return String.valueOf(UserAccessHolder.currentUserId());
    }

    public String operatorName() {
        return UserAccessHolder.currentUserId() == null
                ? null
                : UserAccessHolder.currentUser().getName();
    }
}
