package com.github.thundax.modules.audit.runtime;

import static org.junit.Assert.assertEquals;

import com.github.thundax.common.security.context.SandwishContextHolder;
import com.github.thundax.common.security.context.SandwishSubject;
import com.github.thundax.common.security.context.SandwishSubjectType;
import com.github.thundax.modules.audit.entity.enums.AuditOperatorType;
import java.util.Collections;
import org.junit.After;
import org.junit.Test;

public class AuditOperatorResolverTest {

    private final AuditOperatorResolver resolver = new AuditOperatorResolver();

    @After
    public void tearDown() {
        SandwishContextHolder.clear();
    }

    @Test
    public void shouldResolveAdminUserOperatorType() {
        setSubject(SandwishSubjectType.ADMIN_USER);

        assertEquals(AuditOperatorType.USER, resolver.operatorType());
    }

    @Test
    public void shouldResolveFrontMemberOperatorType() {
        setSubject(SandwishSubjectType.FRONT_MEMBER);

        assertEquals(AuditOperatorType.MEMBER, resolver.operatorType());
    }

    @Test
    public void shouldResolveOpenClientOperatorType() {
        setSubject(SandwishSubjectType.OPEN_CLIENT);

        assertEquals(AuditOperatorType.OPEN_CLIENT, resolver.operatorType());
    }

    @Test
    public void shouldResolveSystemOperatorType() {
        setSubject(SandwishSubjectType.SYSTEM);

        assertEquals(AuditOperatorType.SYSTEM, resolver.operatorType());
    }

    @Test
    public void shouldResolveAnonymousOperatorTypeAsUnknown() {
        assertEquals(AuditOperatorType.UNKNOWN, resolver.operatorType());
    }

    private static void setSubject(SandwishSubjectType subjectType) {
        SandwishContextHolder.setSubject(
                new SandwishSubject("subject-1", subjectType, "subject", "token-1", Collections.emptySet()));
    }
}
