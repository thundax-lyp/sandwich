package com.github.thundax.common.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.util.concurrent.Callable;
import org.junit.After;
import org.junit.Test;

public class SandwishContextHolderTest {

    @After
    public void tearDown() {
        SandwishContextHolder.clear();
    }

    @Test
    public void shouldSetReadAndClearContext() {
        SandwishContext context = new SandwishContext();
        context.setRequestId("request-1");

        SandwishContextHolder.setContext(context);

        assertEquals("request-1", SandwishContextHolder.getContext().getRequestId());

        SandwishContextHolder.clear();

        assertNull(SandwishContextHolder.getContext().getRequestId());
    }

    @Test
    public void shouldCaptureSnapshotAsCopy() {
        SandwishContextHolder.getContext().setUserId("user-1");
        ContextSnapshot snapshot = SandwishContextHolder.snapshot();

        SandwishContextHolder.getContext().setUserId("user-2");
        snapshot.restore();

        assertEquals("user-1", SandwishContextHolder.getContext().getUserId());
        assertNotSame(snapshot, SandwishContextHolder.snapshot());
    }

    @Test
    public void shouldPropagateContextToRunnableAndClearAfterRun() {
        SandwishContextHolder.getContext().setLoginName("admin");
        final String[] loginName = new String[1];

        new ContextAwareRunnable(
                        () -> loginName[0] = SandwishContextHolder.getContext().getLoginName())
                .run();

        assertEquals("admin", loginName[0]);
        assertNull(SandwishContextHolder.getContext().getLoginName());
    }

    @Test
    public void shouldPropagateContextToCallableAndClearAfterCall() throws Exception {
        SandwishContextHolder.getContext().setToken("token");
        Callable<String> callable = () -> SandwishContextHolder.getContext().getToken();

        String token = new ContextAwareCallable<>(callable).call();

        assertEquals("token", token);
        assertNull(SandwishContextHolder.getContext().getToken());
    }
}
