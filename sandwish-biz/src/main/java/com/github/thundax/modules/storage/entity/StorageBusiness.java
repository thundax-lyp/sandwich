package com.github.thundax.modules.storage.entity;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.storage.entity.enums.StorageVisibility;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StorageBusiness {
    private EntityId id;

    private String businessId;
    private String businessType;
    private String businessParams;
    private StorageVisibility visibility = StorageVisibility.PRIVATE;

    public static final String BEAN_NAME = "ResourceBusiness";

    public boolean isPublic() {
        return StorageVisibility.PUBLIC == getVisibility();
    }
}
