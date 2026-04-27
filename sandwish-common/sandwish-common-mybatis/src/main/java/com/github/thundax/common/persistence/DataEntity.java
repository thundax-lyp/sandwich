package com.github.thundax.common.persistence;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.thundax.common.utils.IdGen;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

import java.util.Date;

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
    protected String delFlag;

    public DataEntity() {
        super();
    }

    public DataEntity(String id) {
        super(id);
    }

    @Override
    protected void initialize() {
        this.delFlag = DEL_FLAG_NORMAL;
        this.setPriority(0);
    }

    /**
     * 插入之前执行方法，需要手动调用
     */
    @Override
    public void preInsert() {
        if (StringUtils.isBlank(this.getId())) {
            this.setId(IdGen.uuid());
        }
        this.createDate = new Date();
        this.updateDate = this.createDate;
    }

    /**
     * 更新之前执行方法，需要手动调用
     */
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

    @JsonIgnore
    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    @JsonIgnore
    public boolean isDeleted() {
        return StringUtils.equals(getDelFlag(), DEL_FLAG_DELETE);
    }

}
