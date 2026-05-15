package com.github.thundax.modules.audit.runtime;

import com.github.thundax.common.security.context.SandwishContextHolder;
import com.github.thundax.common.security.context.SandwishSubject;
import com.github.thundax.common.security.context.SandwishSubjectType;
import com.github.thundax.modules.audit.entity.enums.AuditOperatorType;
import org.springframework.stereotype.Component;

@Component
public class AuditOperatorResolver {

    public AuditOperatorType operatorType() {
        SandwishSubjectType subjectType = SandwishContextHolder.currentSubjectType();
        if (subjectType == null) {
            return AuditOperatorType.UNKNOWN;
        }
        switch (subjectType) {
            case ADMIN_USER:
                return AuditOperatorType.USER;
            case FRONT_MEMBER:
                return AuditOperatorType.MEMBER;
            case SYSTEM:
                return AuditOperatorType.SYSTEM;
            case UNKNOWN:
            case ANONYMOUS:
            default:
                return AuditOperatorType.UNKNOWN;
        }
    }

    public String operatorId() {
        return SandwishContextHolder.currentSubjectId();
    }

    public String operatorName() {
        SandwishSubject subject = SandwishContextHolder.currentSubject();
        return subject == null ? null : subject.getDisplayName();
    }
}
