package com.github.thundax.modules.assist.entity.base;

import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.domain.Sortable;
import com.github.thundax.common.id.EntityId;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseAsyncTask implements Auditable, Sortable {
    private EntityId id;

    private String title;

    private String status;
    private String message;
    private String data;

    private Boolean isPrivate;
    private Integer expiredSeconds;
    private int priority;
    private String remarks;
    private Date createDate;
    private Date updateDate;
    private String createUserId;
    private String updateUserId;

    public BaseAsyncTask() {
        initialize();
    }

    protected void initialize() {}

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
