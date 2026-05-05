package com.github.thundax.common.context;

public final class SandwishContextHolder {

    private static final ThreadLocal<SandwishContext> CONTEXT_HOLDER = new ThreadLocal<>();

    private SandwishContextHolder() {}

    public static SandwishContext getContext() {
        SandwishContext context = CONTEXT_HOLDER.get();
        if (context == null) {
            context = new SandwishContext();
            CONTEXT_HOLDER.set(context);
        }
        return context;
    }

    public static void setContext(SandwishContext context) {
        CONTEXT_HOLDER.set(context);
    }

    public static ContextSnapshot snapshot() {
        return ContextSnapshot.capture();
    }

    public static void clear() {
        CONTEXT_HOLDER.remove();
    }
}
