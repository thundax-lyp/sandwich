package com.github.thundax.common.context;

public class ContextSnapshot {

    private final SandwishContext context;

    private ContextSnapshot(SandwishContext context) {
        this.context = context;
    }

    public static ContextSnapshot capture() {
        return new ContextSnapshot(new SandwishContext(SandwishContextHolder.getContext()));
    }

    public void restore() {
        SandwishContextHolder.setContext(new SandwishContext(context));
    }
}
