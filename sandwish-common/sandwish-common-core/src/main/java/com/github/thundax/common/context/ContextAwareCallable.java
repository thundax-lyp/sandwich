package com.github.thundax.common.context;

import java.util.concurrent.Callable;

public class ContextAwareCallable<V> implements Callable<V> {

    private final ContextSnapshot snapshot;
    private final Callable<V> delegate;

    public ContextAwareCallable(Callable<V> delegate) {
        this(ContextSnapshot.capture(), delegate);
    }

    public ContextAwareCallable(ContextSnapshot snapshot, Callable<V> delegate) {
        this.snapshot = snapshot;
        this.delegate = delegate;
    }

    @Override
    public V call() throws Exception {
        try {
            snapshot.restore();
            return delegate.call();
        } finally {
            SandwishRequestContextHolder.clear();
        }
    }
}
