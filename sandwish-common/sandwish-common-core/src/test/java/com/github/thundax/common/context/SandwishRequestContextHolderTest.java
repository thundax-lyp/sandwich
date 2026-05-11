package com.github.thundax.common.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.util.concurrent.Callable;
import org.junit.After;
import org.junit.Test;

public class SandwishRequestContextHolderTest {

    @After
    public void tearDown() {
        SandwishRequestContextHolder.clear();
    }

    @Test
    public void shouldSetReadAndClearContext() {
        SandwishContext context = new SandwishContext();
        context.setRequestId("request-1");

        SandwishRequestContextHolder.setContext(context);

        assertEquals("request-1", SandwishRequestContextHolder.getContext().getRequestId());

        SandwishRequestContextHolder.clear();

        assertNull(SandwishRequestContextHolder.getContext().getRequestId());
    }

    @Test
    public void shouldCaptureSnapshotAsCopy() {
        SandwishRequestContextHolder.getContext().setUserId("user-1");
        ContextSnapshot snapshot = SandwishRequestContextHolder.snapshot();

        SandwishRequestContextHolder.getContext().setUserId("user-2");
        snapshot.restore();

        assertEquals("user-1", SandwishRequestContextHolder.getContext().getUserId());
        assertNotSame(snapshot, SandwishRequestContextHolder.snapshot());
    }

    @Test
    public void shouldPropagateContextToRunnableAndClearAfterRun() {
        SandwishRequestContextHolder.getContext().setLoginName("admin");
        final String[] loginName = new String[1];

        new ContextAwareRunnable(() ->
                        loginName[0] = SandwishRequestContextHolder.getContext().getLoginName())
                .run();

        assertEquals("admin", loginName[0]);
        assertNull(SandwishRequestContextHolder.getContext().getLoginName());
    }

    @Test
    public void shouldPropagateContextToCallableAndClearAfterCall() throws Exception {
        SandwishRequestContextHolder.getContext().setToken("token");
        Callable<String> callable =
                () -> SandwishRequestContextHolder.getContext().getToken();

        String token = new ContextAwareCallable<>(callable).call();

        assertEquals("token", token);
        assertNull(SandwishRequestContextHolder.getContext().getToken());
    }
}
