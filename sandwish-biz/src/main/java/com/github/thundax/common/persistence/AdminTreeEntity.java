package com.github.thundax.common.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.thundax.modules.auth.utils.UserAccessHolder;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.utils.UserServiceHolder;
import com.google.common.collect.Maps;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public abstract class AdminTreeEntity<T> extends TreeEntity<T> implements Signable {

    protected String createUserId;
    protected String updateUserId;

    public AdminTreeEntity() {
        super();
    }

    public AdminTreeEntity(String id) {
        super(id);
    }

    /**
     * 获取名称
     *
     * @return 名称
     */
    public abstract String getName();

    @Override
    public void preInsert() {
        super.preInsert();
        if (this.createUserId == null) {
            this.createUserId = UserAccessHolder.currentUserId();
        }
        this.updateUserId = this.createUserId;
    }

    @Override
    public void preUpdate() {
        super.preUpdate();
        if (this.updateUserId == null) {
            this.updateUserId = UserAccessHolder.currentUserId();
        }
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

    @JsonIgnore
    public User getCreateBy() {
        return UserServiceHolder.get(getCreateUserId());
    }

    public void setCreateBy(User createBy) {
        this.setCreateUserId(createBy == null ? null : createBy.getId());
    }

    @JsonIgnore
    public User getUpdateBy() {
        return UserServiceHolder.get(getUpdateUserId());
    }

    public void setUpdateBy(User updateBy) {
        this.setUpdateUserId(updateBy == null ? null : updateBy.getId());
    }

    public Map<String, Object> toTreeData() {
        return toTreeData(null);
    }

    public Map<String, Object> toTreeData(String idPrefix) {
        if (idPrefix == null) {
            idPrefix = StringUtils.EMPTY;
        }
        Map<String, Object> map = Maps.newHashMap();
        map.put("id", idPrefix + this.getId());
        map.put("pId", this.getParentId() == null ? "0" : idPrefix + this.getParentId());
        map.put("name", this.getName());
        return map;
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
