package com.github.thundax.modules.storage.entity.base;

import com.github.thundax.common.domain.Entity;
import com.github.thundax.common.domain.Sortable;
import com.github.thundax.modules.storage.entity.Storage;
import java.util.Date;
import org.springframework.lang.NonNull;

public abstract class BaseStorage extends Entity<Storage> implements Sortable {

    public BaseStorage() {
        initialize();
    }

    private String name;
    private String extendName;
    private String mimeType;
    private String ownerId;
    private String ownerType;
    private String enableFlag;
    private Integer priority;
    private String remarks;
    private Date createDate;
    private Date updateDate;

    protected void initialize() {
        this.setPriority(0);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtendName() {
        return extendName;
    }

    public void setExtendName(String extendName) {
        this.extendName = extendName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(String ownerType) {
        this.ownerType = ownerType;
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
}
