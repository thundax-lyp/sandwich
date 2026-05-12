package com.github.thundax.common.security.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import org.junit.After;
import org.junit.Test;

public class SandwishContextHolderTest {

    @After
    public void tearDown() {
        SandwishContextHolder.clear();
    }

    @Test
    public void shouldKeepRequestContextSeparateFromSubject() {
        SandwishContextHolder.setRequestId("request-1");
        SandwishContextHolder.setSubject(new SandwishSubject(
                "user-1", SandwishSubjectType.ADMIN_USER, "admin", "token-1", Collections.singleton("sys:user:list")));

        SandwishContextHolder.clearRequestContext();

        assertNull(SandwishContextHolder.requestId());
        assertEquals("user-1", SandwishContextHolder.currentSubjectId());
        assertEquals("token-1", SandwishContextHolder.currentToken());
        assertTrue(SandwishContextHolder.currentAuthorities().contains("sys:user:list"));
    }

    @Test
    public void shouldClearRequestContextAndSubjectTogether() {
        SandwishContextHolder.setRequestId("request-1");
        SandwishContextHolder.setSubject(new SandwishSubject(
                "user-1", SandwishSubjectType.ADMIN_USER, "admin", "token-1", Collections.emptySet()));

        SandwishContextHolder.clear();

        assertNull(SandwishContextHolder.requestId());
        assertNull(SandwishContextHolder.currentSubjectId());
    }

    @Test
    public void shouldReturnAuthoritySnapshot() {
        SandwishSubject subject = new SandwishSubject(
                "user-1", SandwishSubjectType.ADMIN_USER, "admin", "token-1", Arrays.asList("sys:user:list"));

        Set<String> authorities = subject.getAuthorities();
        subject.setAuthorities(Collections.singleton("sys:role:list"));

        assertTrue(authorities.contains("sys:user:list"));
        assertFalse(authorities.contains("sys:role:list"));
    }
}
