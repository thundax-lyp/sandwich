package com.github.thundax.common.thread;

import java.util.HashMap;
import java.util.function.Supplier;

/**
 * @param <T>
 * @author thundax
 */
public class PooledThreadLocal<T> {

    private static final ThreadLocal<PolledThreadLocalMap> THREAD_LOCAL_HOLDER = new ThreadLocal<>();

    public void set(T value) {
        PolledThreadLocalMap map = THREAD_LOCAL_HOLDER.get();
        if (map == null) {
            map = new PolledThreadLocalMap();
            THREAD_LOCAL_HOLDER.set(map);
        }
        map.put(this, value);
    }

    public T get() {
        PolledThreadLocalMap map = THREAD_LOCAL_HOLDER.get();
        if (map != null) {
            @SuppressWarnings("unchecked")
            T result = (T) map.get(this);
            return result;
        }
        return null;
    }

    public T computeIfAbsent(Supplier<T> supplier) {
        T result = get();

        if (result == null && supplier != null) {
            result = supplier.get();
            if (result != null) {
                set(result);
            }
        }

        return result;
    }

    public void remove() {
        PolledThreadLocalMap map = THREAD_LOCAL_HOLDER.get();
        if (map != null) {
            map.remove(this);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public static void reset() {
        THREAD_LOCAL_HOLDER.remove();
    }

    private static class PolledThreadLocalMap extends HashMap<PooledThreadLocal<?>, Object> {}
}
