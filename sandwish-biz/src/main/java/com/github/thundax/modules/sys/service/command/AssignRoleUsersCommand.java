package com.github.thundax.modules.sys.service.command;

import com.github.thundax.modules.sys.entity.valueobject.RoleId;
import com.github.thundax.modules.sys.entity.valueobject.UserId;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignRoleUsersCommand {
    private RoleId roleId;
    private List<UserId> userIds;
}
