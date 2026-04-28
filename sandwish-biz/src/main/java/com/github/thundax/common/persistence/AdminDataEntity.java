package com.github.thundax.common.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * 管理后台DataEntity
 *
 * @param <T>
 * @author wdit
 */
@JsonInclude(Include.NON_NULL)
public abstract class AdminDataEntity<T> extends DataEntity<T> implements Signable {

    private static final long serialVersionUID = 1L;

    private String createUserId;
    private String updateUserId;

    public AdminDataEntity() {
        super();
    }

    public AdminDataEntity(String id) {
        super(id);
    }

    public String getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(String createUserId) {
        this.createUserId = createUserId;
    }

    public String getUpdateUserId() {
        return updateUserId;
    }

    public void setUpdateUserId(String updateUserId) {
        this.updateUserId = updateUserId;
    }

    @Override
    public void preInsert() {
        super.preInsert();
    }

    @Override
    public void preUpdate() {
        super.preUpdate();
    }

    @Override
    @JsonIgnore
    public String getSignName() {
        return null;
    }

    @Override
    @JsonIgnore
    public String getSignId() {
        return getId();
    }

    @Override
    @JsonIgnore
    public String getSignBody() {
        return null;
    }
}
