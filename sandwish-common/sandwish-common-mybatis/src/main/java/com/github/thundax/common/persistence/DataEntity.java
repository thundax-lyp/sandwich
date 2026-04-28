package com.github.thundax.common.persistence;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import org.springframework.lang.NonNull;

/**
 * 数据Entity类
 *
 * @author thundax
 */
public abstract class DataEntity<T> extends BaseEntity<T> {

    private static final long serialVersionUID = 1L;

    protected Integer priority;
    protected String remarks;
    protected Date createDate;
    protected Date updateDate;

    public DataEntity() {
        super();
    }

    public DataEntity(String id) {
        super(id);
    }

    @Override
    protected void initialize() {
        this.setPriority(0);
    }

    @Override
    public void preInsert() {
        this.createDate = new Date();
        this.updateDate = this.createDate;
    }

    @Override
    public void preUpdate() {
        this.updateDate = new Date();
    }

    @NonNull
    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        if (priority != null && priority >= 0) {
            this.priority = priority;
        } else {
            this.setPriority(0);
        }
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
}
