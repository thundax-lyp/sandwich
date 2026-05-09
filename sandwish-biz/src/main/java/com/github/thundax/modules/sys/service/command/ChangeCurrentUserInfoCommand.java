package com.github.thundax.modules.sys.service.command;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.entity.enums.UserPrivilege;
import com.github.thundax.modules.sys.entity.enums.UserStatus;
import com.github.thundax.modules.sys.entity.valueobject.AccessRank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeCurrentUserInfoCommand {
    private EntityId userId;
    private EntityId departmentId;
    private String email;
    private String mobile;
    private String tel;
    private String name;
    private AccessRank rank;
    private UserPrivilege privilege;
    private UserStatus status;
    private int priority;
    private String remarks;
}
