package com.github.thundax.common.domain;

/**
 * 排序字段承载能力。
 */
public interface Sortable {

    Integer getPriority();

    void setPriority(Integer priority);
}
