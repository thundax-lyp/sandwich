package com.github.thundax.modules.sys.service.command;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.entity.enums.RolePrivilege;
import com.github.thundax.modules.sys.entity.enums.RoleStatus;
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
    private EntityId id;
    private String name;
    private RolePrivilege privilege;
    private RoleStatus status;
    private int priority;
    private String remarks;
    private List<EntityId> menuIdList;
}
