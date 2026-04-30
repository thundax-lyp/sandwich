package com.github.thundax.modules.sys.entity.base;

import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.domain.Signable;
import com.github.thundax.common.domain.Sortable;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseRole implements Auditable, Signable, Sortable {
    private EntityId id;

    public BaseRole() {
        initialize();
    }

    private String name;
    private String adminFlag;
    private String enableFlag;
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

    @Override
    public String getSignId() {
        return EntityIdCodec.toValue(getId());
    }
}
