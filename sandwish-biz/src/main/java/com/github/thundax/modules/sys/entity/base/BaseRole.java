package com.github.thundax.modules.sys.entity.base;

import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.domain.Entity;
import com.github.thundax.common.domain.Signable;
import com.github.thundax.common.domain.Sortable;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.sys.entity.Role;
import java.util.Date;
import org.springframework.lang.NonNull;

public abstract class BaseRole extends Entity<Role> implements Auditable, Signable, Sortable {

    public BaseRole() {
        initialize();
    }

    private String name;
    private String adminFlag;
    private String enableFlag;
    private Integer priority;
    private String remarks;
    private Date createDate;
    private Date updateDate;
    private String createUserId;
    private String updateUserId;

    protected void initialize() {
        this.setPriority(0);
    }

    public String getId() {
        return EntityIdCodec.toValue(getEntityId());
    }

    public void setId(String id) {
        setEntityId(EntityIdCodec.toDomain(id));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdminFlag() {
        return adminFlag;
    }

    public void setAdminFlag(String adminFlag) {
        this.adminFlag = adminFlag;
    }

    public String getEnableFlag() {
        return enableFlag;
    }

    public void setEnableFlag(String enableFlag) {
        this.enableFlag = enableFlag;
    }

    @Override
    @NonNull
    public Integer getPriority() {
        return priority == null ? 0 : priority;
    }

    @Override
    public void setPriority(Integer priority) {
        this.priority = priority != null && priority >= 0 ? priority : 0;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    @Override
    public String getCreateUserId() {
        return createUserId;
    }

    @Override
    public void setCreateUserId(String createUserId) {
        this.createUserId = createUserId;
    }

    @Override
    public String getUpdateUserId() {
        return updateUserId;
    }

    @Override
    public void setUpdateUserId(String updateUserId) {
        this.updateUserId = updateUserId;
    }

    @Override
    public String getSignId() {
        return getId();
    }
}
