package com.github.thundax.common.test.architecture.fixture.concurrency.invalid;

import java.util.concurrent.Executors;

public class ExecutorsFactoryFixture {

    public void run() {
        Executors.newSingleThreadExecutor();
    }
}
