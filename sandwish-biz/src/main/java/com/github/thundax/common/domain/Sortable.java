package com.github.thundax.common.domain;

/**
 * 排序字段承载能力。
 * <p>
 * 所有排序都通过服务端维护的 {@link #getPriority()} 与 {@link #setPriority(int)}
 * 完成，外部接口不得提交 priority 数值。
 */
public interface Sortable {

    int getPriority();

    void setPriority(int priority);
}
