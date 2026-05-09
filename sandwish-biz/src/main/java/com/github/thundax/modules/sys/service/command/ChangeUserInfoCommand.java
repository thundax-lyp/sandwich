package com.github.thundax.modules.sys.service.command;

import com.github.thundax.modules.sys.entity.enums.UserPrivilege;
import com.github.thundax.modules.sys.entity.enums.UserStatus;
import com.github.thundax.modules.sys.entity.valueobject.AccessRank;
import com.github.thundax.modules.sys.entity.valueobject.DepartmentId;
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
public class ChangeUserInfoCommand {
    private UserId id;
    private DepartmentId departmentId;
    private String email;
    private String mobile;
    private String tel;
    private String name;
    private AccessRank rank;
    private UserPrivilege privilege;
    private UserStatus status;
    private int priority;
    private String remarks;
    private String loginName;
    private List<RoleId> roleIdList;
}
