package com.github.thundax.modules.storage.entity;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.storage.entity.enums.StorageVisibility;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

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

    public void setVisibility(String visibility) {
        this.visibility = StringUtils.isBlank(visibility) ? null : StorageVisibility.from(visibility);
    }

    public void setVisibility(StorageVisibility visibility) {
        this.visibility = visibility;
    }

    public boolean isPublic() {
        return StorageVisibility.PUBLIC == getVisibility();
    }
}
