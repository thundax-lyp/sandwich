package com.github.thundax.common.utils.function;

/**
 * copy from java.util.function.Supplier
 *
 * @param <T>
 * @author thundax
 */
@FunctionalInterface
public interface ThrowableSupplier<T> {

    /**
     * Gets a result.
     *
     * @return a result
     * @throws Exception throwable
     */
    T get() throws Exception;


}
