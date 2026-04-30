package com.github.thundax.common.domain;

/**
 * 排序字段承载能力。
 */
public interface Sortable {

    int getPriority();

    void setPriority(int priority);
}
