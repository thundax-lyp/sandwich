package com.github.thundax.common.utils.function;

import java.util.Objects;

/**
 * copy from java.util.function.BiConsumer
 *
 * @param <T>
 * @param <U>
 * @author thundax
 */
@FunctionalInterface
public interface ThrowableBiConsumer<T, U> {

    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     * @throws Exception Exception
     */
    void accept(T t, U u) throws Exception;

    /**
     * Returns a composed {@code BiConsumer} that performs, in sequence, this operation followed by
     * the {@code after} operation. If performing either operation throws an exception, it is
     * relayed to the caller of the composed operation. If performing this operation throws an
     * exception, the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code BiConsumer} that performs in sequence this operation followed by
     *     the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    default ThrowableBiConsumer<T, U> andThen(ThrowableBiConsumer<? super T, ? super U> after) {
        Objects.requireNonNull(after);

        return (l, r) -> {
            accept(l, r);
            after.accept(l, r);
        };
    }
}
