package com.github.thundax.modules.sys.service.command;

import com.github.thundax.modules.sys.entity.enums.RoleStatus;
import com.github.thundax.modules.sys.entity.valueobject.RoleId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRoleStatusCommand {
    private RoleId id;
    private RoleStatus status;
}
