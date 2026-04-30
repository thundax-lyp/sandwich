package com.github.thundax.common.utils.function;

import java.util.Objects;

/**
 * 允许抛出异常的 BiConsumer 变体。
 *
 * @param <T> 第一个输入类型
 * @param <U> 第二个输入类型
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
