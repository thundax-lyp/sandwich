package com.github.thundax.common.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.utils.UserServiceHolder;
import com.google.common.collect.Maps;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public abstract class AdminTreeEntity<T extends AdminTreeEntity<T>> extends TreeEntity<T> implements Signable {

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
        return UserServiceHolder.get(EntityIdCodec.toDomain(getCreateUserId()));
    }

    public void setCreateBy(User createBy) {
        this.setCreateUserId(createBy == null ? null : EntityIdCodec.toValue(createBy.getEntityId()));
    }

    @JsonIgnore
    public User getUpdateBy() {
        return UserServiceHolder.get(EntityIdCodec.toDomain(getUpdateUserId()));
    }

    public void setUpdateBy(User updateBy) {
        this.setUpdateUserId(updateBy == null ? null : EntityIdCodec.toValue(updateBy.getEntityId()));
    }

    public Map<String, Object> toTreeData() {
        return toTreeData(null);
    }

    public Map<String, Object> toTreeData(String idPrefix) {
        if (idPrefix == null) {
            idPrefix = StringUtils.EMPTY;
        }
        Map<String, Object> map = Maps.newHashMap();
        map.put("id", idPrefix + EntityIdCodec.toValue(this.getEntityId()));
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
        return EntityIdCodec.toValue(getEntityId());
    }

    @Override
    @JsonIgnore
    public String getSignBody() {
        return null;
    }
}
