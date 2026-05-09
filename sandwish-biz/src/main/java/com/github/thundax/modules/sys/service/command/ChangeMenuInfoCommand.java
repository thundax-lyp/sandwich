package com.github.thundax.modules.sys.service.command;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.entity.enums.MenuVisibility;
import com.github.thundax.modules.sys.entity.valueobject.AccessRank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeMenuInfoCommand {
    private EntityId id;
    private EntityId parentId;
    private String name;
    private String perms;
    private AccessRank rank;
    private MenuVisibility visibility;
    private String displayParams;
    private String url;
    private String target;
    private int priority;
    private String remarks;
}
