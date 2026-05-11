package com.github.thundax.modules.audit.runtime;

import com.github.thundax.common.security.context.SandwishContextHolder;
import com.github.thundax.modules.audit.entity.enums.AuditOperatorType;
import org.springframework.stereotype.Component;

@Component
public class AuditOperatorResolver {

    public AuditOperatorType operatorType() {
        return SandwishContextHolder.currentSubjectId() == null ? AuditOperatorType.UNKNOWN : AuditOperatorType.USER;
    }

    public String operatorId() {
        return SandwishContextHolder.currentSubjectId();
    }

    public String operatorName() {
        return SandwishContextHolder.currentSubject().getDisplayName();
    }
}
