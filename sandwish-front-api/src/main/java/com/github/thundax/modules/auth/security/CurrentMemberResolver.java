package com.github.thundax.modules.auth.security;

import com.github.thundax.common.security.context.SandwishContextHolder;
import com.github.thundax.common.security.context.SandwishSubject;
import com.github.thundax.common.security.context.SandwishSubjectType;
import org.springframework.stereotype.Component;

@Component
public class CurrentMemberResolver {

    public MemberSpringPrincipal currentPrincipal() {
        String memberId = currentMemberId();
        return memberId == null ? null : new MemberSpringPrincipal(memberId);
    }

    public String currentMemberId() {
        SandwishSubject subject = SandwishContextHolder.currentSubject();
        return subject.getSubjectType() == SandwishSubjectType.FRONT_MEMBER ? subject.getSubjectId() : null;
    }

    public String currentToken() {
        SandwishSubject subject = SandwishContextHolder.currentSubject();
        return subject.getSubjectType() == SandwishSubjectType.FRONT_MEMBER ? subject.getToken() : null;
    }
}
