package com.github.thundax.modules.assist.entity.base;

import com.github.thundax.common.persistence.AdminDataEntity;
import com.github.thundax.modules.assist.entity.AsyncTask;

public class BaseAsyncTask extends AdminDataEntity<AsyncTask> {

    private String title;

    private String status;
    private String message;
    private String data;

    private Boolean isPrivate;
    private Integer expiredSeconds;

    public BaseAsyncTask() {}

    public BaseAsyncTask(String id) {
        setId(id);
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
}
