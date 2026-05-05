package com.github.thundax.common.context;

public class ContextAwareRunnable implements Runnable {

    private final ContextSnapshot snapshot;
    private final Runnable delegate;

    public ContextAwareRunnable(Runnable delegate) {
        this(ContextSnapshot.capture(), delegate);
    }

    public ContextAwareRunnable(ContextSnapshot snapshot, Runnable delegate) {
        this.snapshot = snapshot;
        this.delegate = delegate;
    }

    @Override
    public void run() {
        try {
            snapshot.restore();
            delegate.run();
        } finally {
            SandwishContextHolder.clear();
        }
    }
}
