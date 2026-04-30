package com.github.thundax.common.domain;

/**
 * 审计字段承载能力。
 */
public interface Auditable {

    String getCreateUserId();

    void setCreateUserId(String createUserId);

    String getUpdateUserId();

    void setUpdateUserId(String updateUserId);
}
