package com.github.thundax.modules.storage.entity.base;

import com.github.thundax.common.config.Global;
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
public abstract class BaseStorage implements Sortable {
    private EntityId id;
    private String name;
    private String extendName;
    private String mimeType;
    private String ownerId;
    private String ownerType;
    private String enableFlag = Global.ENABLE;
    private int priority;
    private String remarks;
    private Date createDate;
    private Date updateDate;

    @Override
    public void setPriority(int priority) {
        this.priority = priority >= 0 ? priority : 0;
    }
}
