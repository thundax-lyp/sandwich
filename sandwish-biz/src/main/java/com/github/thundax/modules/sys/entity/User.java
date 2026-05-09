package com.github.thundax.modules.sys.entity;

import com.github.thundax.common.domain.Sortable;
import com.github.thundax.modules.sys.entity.enums.UserPrivilege;
import com.github.thundax.modules.sys.entity.enums.UserStatus;
import com.github.thundax.modules.sys.entity.valueobject.AccessRank;
import com.github.thundax.modules.sys.entity.valueobject.DepartmentId;
import com.github.thundax.modules.sys.entity.valueobject.UserId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;

/**
 * 后台用户主体。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User implements Sortable {
    private UserId id;

    private DepartmentId departmentId;

    private String email;
    private String mobile;
    private String tel;
    private String name;
    private AccessRank rank = AccessRank.of(0);

    private UserPrivilege privilege = UserPrivilege.NORMAL;
    private UserStatus status;
    private int priority;
    private String remarks;

    @NonNull
    public AccessRank getRank() {
        return rank == null ? AccessRank.of(null) : rank;
    }

    public void setRank(AccessRank rank) {
        this.rank = rank == null ? AccessRank.of(null) : rank;
    }

    public boolean isSuper() {
        return UserPrivilege.SUPER == getPrivilege();
    }

    public boolean isAdmin() {
        return UserPrivilege.ADMIN == getPrivilege();
    }

    public boolean isEnable() {
        return UserStatus.ENABLED == getStatus();
    }
}
