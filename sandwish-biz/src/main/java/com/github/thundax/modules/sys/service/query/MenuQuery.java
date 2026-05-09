package com.github.thundax.modules.sys.service.query;

import com.github.thundax.modules.sys.entity.enums.MenuVisibility;
import com.github.thundax.modules.sys.entity.valueobject.AccessRank;
import com.github.thundax.modules.sys.entity.valueobject.MenuId;
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
    private List<MenuId> ids;
    private MenuId childId;
    private MenuId ancestorId;
    private MenuId parentId;
    private MenuVisibility visibility;
    private AccessRank maxRank;
}
