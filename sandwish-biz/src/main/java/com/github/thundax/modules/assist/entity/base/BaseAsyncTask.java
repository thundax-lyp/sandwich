package com.github.thundax.modules.assist.entity.base;

import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.domain.Entity;
import com.github.thundax.common.domain.Sorted;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.assist.entity.AsyncTask;
import java.util.Date;
import org.springframework.lang.NonNull;

public class BaseAsyncTask extends Entity<AsyncTask> implements Auditable, Sorted {

    private String title;

    private String status;
    private String message;
    private String data;

    private Boolean isPrivate;
    private Integer expiredSeconds;
    private Integer priority;
    private String remarks;
    private Date createDate;
    private Date updateDate;
    private String createUserId;
    private String updateUserId;

    public BaseAsyncTask() {
        initialize();
    }

    public BaseAsyncTask(String id) {
        this();
        setEntityId(EntityIdCodec.toDomain(id));
    }

    protected void initialize() {
        this.setPriority(0);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Boolean getPrivate() {
        return isPrivate;
    }

    public void setPrivate(Boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public Integer getExpiredSeconds() {
        return expiredSeconds;
    }

    public void setExpiredSeconds(Integer expiredSeconds) {
        this.expiredSeconds = expiredSeconds;
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
}
