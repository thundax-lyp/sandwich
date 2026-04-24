package com.github.thundax.modules.assist.persistence.dataobject;

import com.github.thundax.common.persistence.AdminDataEntity;
import lombok.NoArgsConstructor;

/**
 * 异步任务持久化对象。
 */
@NoArgsConstructor
public class AsyncTaskDO extends AdminDataEntity<AsyncTaskDO> {

    private String title;
    private String status;
    private String message;
    private String data;
    private Boolean isPrivate;
    private Integer expiredSeconds;

    public AsyncTaskDO(String id) {
        super(id);
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
