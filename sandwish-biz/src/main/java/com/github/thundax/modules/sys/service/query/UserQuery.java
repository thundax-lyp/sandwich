package com.github.thundax.modules.sys.service.query;

import com.github.thundax.modules.sys.entity.enums.UserPrivilege;
import com.github.thundax.modules.sys.entity.enums.UserStatus;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class UserQuery implements Serializable {
    private String officeId;
    private String loginName;
    private String name;
    private UserStatus status;
    private UserPrivilege privilege;
    private String orderBy;

    public void setStatus(String status) {
        this.status = StringUtils.isBlank(status) ? null : UserStatus.from(status);
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public void setPrivilege(String privilege) {
        this.privilege = StringUtils.isBlank(privilege) ? null : UserPrivilege.from(privilege);
    }

    public void setPrivilege(UserPrivilege privilege) {
        this.privilege = privilege;
    }
}
