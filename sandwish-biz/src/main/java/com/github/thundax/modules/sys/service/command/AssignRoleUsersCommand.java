package com.github.thundax.modules.sys.service.command;

import com.github.thundax.common.id.EntityId;
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
    private EntityId roleId;
    private List<EntityId> userIds;
}
