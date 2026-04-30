package com.github.thundax.modules.assist.entity.base;

import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.domain.Sortable;
import com.github.thundax.common.id.EntityId;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BaseAsyncTask implements Auditable, Sortable {
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
}
