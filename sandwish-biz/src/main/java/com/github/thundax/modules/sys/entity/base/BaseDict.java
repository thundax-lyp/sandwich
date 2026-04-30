package com.github.thundax.modules.sys.entity.base;

import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.domain.Sortable;
import com.github.thundax.common.id.EntityId;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseDict implements Auditable, Sortable {
    private EntityId id;

    public BaseDict() {
        initialize();
    }

    private String type;
    private String label;
    private String value;
    private int priority;
    private String remarks;
    private Date createDate;
    private Date updateDate;
    private String createUserId;
    private String updateUserId;

    protected void initialize() {}

    @Override
    public void setPriority(int priority) {
        this.priority = priority >= 0 ? priority : 0;
    }
}
