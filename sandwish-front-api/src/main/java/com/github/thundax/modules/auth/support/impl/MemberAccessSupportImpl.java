package com.github.thundax.modules.auth.support.impl;

import com.github.thundax.common.security.context.SandwishContextHolder;
import com.github.thundax.common.security.context.SandwishSubjectType;
import com.github.thundax.modules.auth.support.MemberAccessSupport;
import org.springframework.stereotype.Component;

@Component
public class MemberAccessSupportImpl implements MemberAccessSupport {

    @Override
    public String getCurrentMemberId() {
        return SandwishContextHolder.currentSubjectType() == SandwishSubjectType.FRONT_MEMBER
                ? SandwishContextHolder.currentSubjectId()
                : null;
    }
}
