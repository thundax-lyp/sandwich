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
public abstract class BaseMenu implements Auditable, Signable, Sortable {
    private EntityId id;

    public static final String ROOT_ID = "ROOT";

    private String parentId;

    private String name;
    private String perms;
    private Integer ranks;
    private String displayFlag;
    private String displayParams;
    private String url;
    private String target;
    private int priority;
    private String remarks;
    private Date createDate;
    private Date updateDate;
    private String createUserId;
    private String updateUserId;

    public BaseMenu() {
        initialize();
    }

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
