package com.github.thundax.common.utils.function;

import java.util.Objects;

/**
 * copy from java.util.function.Predicate
 *
 * @param <T>
 * @author thundax
 */
@FunctionalInterface
public interface ThrowablePredicate<T> {

    /**
     * Evaluates this predicate on the given argument.
     *
     * @param t the input argument
     * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
     * @throws Exception throwable
     */
    boolean test(T t) throws Exception;

    /**
     * Returns a composed predicate that represents a short-circuiting logical AND of this predicate
     * and another. When evaluating the composed predicate, if this predicate is {@code false}, then
     * the {@code other} predicate is not evaluated.
     *
     * <p>Any exceptions thrown during evaluation of either predicate are relayed to the caller; if
     * evaluation of this predicate throws an exception, the {@code other} predicate will not be
     * evaluated.
     *
     * @param other a predicate that will be logically-ANDed with this predicate
     * @return a composed predicate that represents the short-circuiting logical AND of this
     *     predicate and the {@code other} predicate
     * @throws NullPointerException if other is null
     */
    default ThrowablePredicate<T> and(ThrowablePredicate<? super T> other) {
        Objects.requireNonNull(other);
        return (t) -> test(t) && other.test(t);
    }

    /**
     * Returns a predicate that represents the logical negation of this predicate.
     *
     * @return a predicate that represents the logical negation of this predicate
     */
    default ThrowablePredicate<T> negate() {
        return (t) -> !test(t);
    }

    /**
     * Returns a composed predicate that represents a short-circuiting logical OR of this predicate
     * and another. When evaluating the composed predicate, if this predicate is {@code true}, then
     * the {@code other} predicate is not evaluated.
     *
     * <p>Any exceptions thrown during evaluation of either predicate are relayed to the caller; if
     * evaluation of this predicate throws an exception, the {@code other} predicate will not be
     * evaluated.
     *
     * @param other a predicate that will be logically-ORed with this predicate
     * @return a composed predicate that represents the short-circuiting logical OR of this
     *     predicate and the {@code other} predicate
     * @throws NullPointerException if other is null
     */
    default ThrowablePredicate<T> or(ThrowablePredicate<? super T> other) {
        Objects.requireNonNull(other);
        return (t) -> test(t) || other.test(t);
    }
}
