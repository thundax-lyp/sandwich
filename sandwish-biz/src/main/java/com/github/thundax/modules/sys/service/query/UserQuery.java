package com.github.thundax.modules.sys.service.query;

import com.github.thundax.modules.sys.entity.enums.UserPrivilege;
import com.github.thundax.modules.sys.entity.enums.UserStatus;
import com.github.thundax.modules.sys.entity.valueobject.DepartmentId;
import com.github.thundax.modules.sys.entity.valueobject.UserId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserQuery {
    private UserId id;
    private DepartmentId departmentId;
    private String loginName;
    private String name;
    private UserStatus status;
    private UserPrivilege privilege;
    private String orderBy;
}
