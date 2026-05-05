package com.github.thundax.common.test.architecture.fixture.concurrency.invalid;

public class RawThreadFixture {

    public void run() {
        new Thread(() -> {}).start();
    }
}
