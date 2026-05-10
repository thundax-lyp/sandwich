package com.github.thundax.modules.sys.service.command;

import com.github.thundax.modules.sys.entity.enums.MenuVisibility;
import com.github.thundax.modules.sys.entity.valueobject.AccessRank;
import com.github.thundax.modules.sys.entity.valueobject.MenuId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeMenuInfoCommand {
    private MenuId id;
    private MenuId parentId;
    private String name;
    private String perms;
    private AccessRank rank;
    private MenuVisibility visibility;
    private String displayParams;
    private String url;
    private String target;
    private String remarks;
}
