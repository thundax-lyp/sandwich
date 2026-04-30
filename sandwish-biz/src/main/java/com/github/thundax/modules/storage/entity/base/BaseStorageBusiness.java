package com.github.thundax.modules.storage.entity.base;

import com.github.thundax.common.config.Global;
import com.github.thundax.common.id.EntityId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseStorageBusiness {
    private EntityId id;

    private String businessId;
    private String businessType;
    private String businessParams;
    private String publicFlag = Global.NO;
}
