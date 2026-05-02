package com.github.thundax.modules.sys.service.query;

import com.github.thundax.modules.sys.entity.enums.UserPrivilege;
import com.github.thundax.modules.sys.entity.enums.UserStatus;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserQuery implements Serializable {
    private String officeId;
    private String loginName;
    private String name;
    private UserStatus status;
    private UserPrivilege privilege;
    private String orderBy;
}
