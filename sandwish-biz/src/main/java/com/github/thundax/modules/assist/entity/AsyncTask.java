package com.github.thundax.modules.assist.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import com.github.thundax.modules.assist.entity.base.BaseAsyncTask;
import com.github.thundax.modules.sys.entity.User;
import java.util.Objects;
import org.springframework.lang.NonNull;

/** @author wdit */
public class AsyncTask extends BaseAsyncTask {

    public static final int DEFAULT_EXPIRED_SECONDS = 1800;

    /** 开始，激活，挂起，结束 */
    public static final String STATUS_IDLE = "idle";

    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_SUSPENDED = "suspended";
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_ERROR = "error";

    public AsyncTask() {
        super();
    }

    public AsyncTask(String id) {
        super(id);
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.setStatus(STATUS_IDLE);
        this.setPrivate(false);
        this.setExpiredSeconds(DEFAULT_EXPIRED_SECONDS);
    }

    @Override
    @NonNull
    public Integer getExpiredSeconds() {
        Integer expiredSeconds = super.getExpiredSeconds();
        return expiredSeconds != null ? expiredSeconds : DEFAULT_EXPIRED_SECONDS;
    }

    @JsonIgnore
    public boolean isPrivate() {
        return Boolean.TRUE.equals(getPrivate());
    }

    @JsonIgnore
    public boolean isActive() {
        return StringUtils.equals(STATUS_ACTIVE, getStatus());
    }

    @JsonIgnore
    public boolean isSuspended() {
        return StringUtils.equals(STATUS_SUSPENDED, getStatus());
    }

    @JsonIgnore
    public boolean isSuccess() {
        return StringUtils.equals(STATUS_SUCCESS, getStatus());
    }

    public boolean isBelongTo(User user) {
        return user != null && Objects.equals(user.getId(), getCreateUserId());
    }
}
