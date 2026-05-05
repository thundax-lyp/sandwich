package com.github.thundax.common.test.architecture.fixture.concurrency.valid;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ManagedExecutorFixture {

    public void run(Executor executor) {
        CompletableFuture.runAsync(() -> {}, executor);
    }
}
