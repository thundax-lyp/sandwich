package com.github.thundax.common.context;

public class ContextSnapshot {

    private final SandwishContext context;

    private ContextSnapshot(SandwishContext context) {
        this.context = context;
    }

    public static ContextSnapshot capture() {
        return new ContextSnapshot(new SandwishContext(SandwishRequestContextHolder.getContext()));
    }

    public void restore() {
        SandwishRequestContextHolder.setContext(new SandwishContext(context));
    }
}
