package com.github.thundax.modules.sys.service.query;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.entity.enums.MenuVisibility;
import com.github.thundax.modules.sys.entity.valueobject.AccessRank;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuQuery {
    private EntityId id;
    private List<EntityId> ids;
    private EntityId childId;
    private EntityId ancestorId;
    private EntityId parentId;
    private MenuVisibility visibility;
    private AccessRank maxRank;
}
