package com.github.thundax.common.domain;

public interface Auditable {

    String getCreateUserId();

    void setCreateUserId(String createUserId);

    String getUpdateUserId();

    void setUpdateUserId(String updateUserId);
}
