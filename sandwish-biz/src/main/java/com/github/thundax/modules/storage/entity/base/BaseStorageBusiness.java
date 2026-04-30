package com.github.thundax.modules.storage.entity.base;

import com.github.thundax.common.id.EntityId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseStorageBusiness {
    private EntityId id;

    private String businessId;
    private String businessType;
    private String businessParams;
    private String publicFlag;

    public BaseStorageBusiness() {
        initialize();
    }

    protected void initialize() {}
}
