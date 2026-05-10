package com.github.thundax.modules.sys.service.command;

import com.github.thundax.modules.sys.entity.enums.RolePrivilege;
import com.github.thundax.modules.sys.entity.enums.RoleStatus;
import com.github.thundax.modules.sys.entity.valueobject.MenuId;
import com.github.thundax.modules.sys.entity.valueobject.RoleId;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRoleInfoCommand {
    private RoleId id;
    private String name;
    private RolePrivilege privilege;
    private RoleStatus status;
    private String remarks;
    private List<MenuId> menuIdList;
}
