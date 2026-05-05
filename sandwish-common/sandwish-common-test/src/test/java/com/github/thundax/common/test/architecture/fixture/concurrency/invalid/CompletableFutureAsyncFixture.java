package com.github.thundax.common.test.architecture.fixture.concurrency.invalid;

import java.util.concurrent.CompletableFuture;

public class CompletableFutureAsyncFixture {

    public void run() {
        CompletableFuture.runAsync(() -> {});
    }
}
