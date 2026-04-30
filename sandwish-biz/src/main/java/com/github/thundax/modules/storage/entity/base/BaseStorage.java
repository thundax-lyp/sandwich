package com.github.thundax.modules.storage.entity.base;

import com.github.thundax.common.domain.Sortable;
import com.github.thundax.common.id.EntityId;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseStorage implements Sortable {
    private EntityId id;

    public BaseStorage() {
        initialize();
    }

    private String name;
    private String extendName;
    private String mimeType;
    private String ownerId;
    private String ownerType;
    private String enableFlag;
    private int priority;
    private String remarks;
    private Date createDate;
    private Date updateDate;

    protected void initialize() {}

    @Override
    public void setPriority(int priority) {
        this.priority = priority >= 0 ? priority : 0;
    }
}
