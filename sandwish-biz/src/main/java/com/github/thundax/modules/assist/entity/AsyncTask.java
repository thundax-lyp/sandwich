package com.github.thundax.modules.assist.entity;

import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.domain.Sortable;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.sys.entity.User;
import java.util.Date;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AsyncTask implements Auditable, Sortable {
    public static final int DEFAULT_EXPIRED_SECONDS = 1800;

    public static final String STATUS_IDLE = "idle";

    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_SUSPENDED = "suspended";
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_ERROR = "error";

    private EntityId id;

    private String title;

    private String status = STATUS_IDLE;
    private String message;
    private String data;

    private Boolean isPrivate = false;
    private Integer expiredSeconds = DEFAULT_EXPIRED_SECONDS;
    private int priority;
    private String remarks;
    private Date createDate;
    private Date updateDate;
    private String createUserId;
    private String updateUserId;

    public Boolean getPrivate() {
        return isPrivate;
    }

    public void setPrivate(Boolean aPrivate) {
        isPrivate = aPrivate;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority >= 0 ? priority : 0;
    }

    @NonNull
    public Integer getExpiredSeconds() {
        Integer expiredSeconds = this.expiredSeconds;
        return expiredSeconds != null ? expiredSeconds : DEFAULT_EXPIRED_SECONDS;
    }

    public boolean isPrivate() {
        return Boolean.TRUE.equals(getPrivate());
    }

    public boolean isActive() {
        return StringUtils.equals(STATUS_ACTIVE, getStatus());
    }

    public boolean isSuspended() {
        return StringUtils.equals(STATUS_SUSPENDED, getStatus());
    }

    public boolean isSuccess() {
        return StringUtils.equals(STATUS_SUCCESS, getStatus());
    }

    public boolean isBelongTo(User user) {
        return user != null && Objects.equals(EntityIdCodec.toValue(user.getId()), getCreateUserId());
    }
}
