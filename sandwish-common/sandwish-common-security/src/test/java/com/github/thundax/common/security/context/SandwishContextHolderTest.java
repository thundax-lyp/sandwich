package com.github.thundax.common.security.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
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
}
