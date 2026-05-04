package com.github.thundax.modules.sys.entity;

import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.domain.Signable;
import com.github.thundax.common.domain.Sortable;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.utils.JsonUtils;
import com.github.thundax.modules.sys.entity.enums.UserPrivilege;
import com.github.thundax.modules.sys.entity.enums.UserStatus;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

/**
 * 后台用户主体。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User implements Auditable, Signable, Sortable {
    private EntityId id;

    private String departmentId;

    private String loginName;

    private String loginPass;

    private String email;
    private String mobile;
    private String tel;
    private String name;
    private Integer ranks = 0;

    private Date registerDate;
    private String registerIp;

    private Date lastLoginDate;
    private String lastLoginIp;
    private Integer loginCount = 0;

    private UserPrivilege privilege = UserPrivilege.NORMAL;
    private UserStatus status;
    private String ssoLoginName;

    private int priority;
    private String remarks;
    private Date createDate;
    private Date updateDate;
    private String createUserId;
    private String updateUserId;

    @Override
    public String getSignId() {
        return EntityIdCodec.toValue(getId());
    }

    public static final String BEAN_NAME = "User";

    public static final int MAX_RANKS = 9;

    private List<String> roleIdList;

    @NonNull
    public Integer getRanks() {
        Integer ranks = this.ranks;
        if (ranks == null || ranks < 0) {
            return 0;
        } else if (ranks >= MAX_RANKS) {
            return MAX_RANKS;
        } else {
            return ranks;
        }
    }

    public void setDepartment(Department department) {
        this.setDepartmentId(department == null ? null : EntityIdCodec.toValue(department.getId()));
    }

    public boolean isBelongTo(Department department) {
        return department != null && Objects.equals(this.getDepartmentId(), EntityIdCodec.toValue(department.getId()));
    }

    @NotNull
    public List<String> getRoleIdList() {
        if (this.roleIdList == null) {
            this.roleIdList = new ArrayList<>();
        }
        return this.roleIdList;
    }

    public void setRoleIdList(List<String> roleIdList) {
        this.roleIdList = roleIdList;
    }

    public void setRoleList(List<Role> roleList) {
        this.roleIdList = roleList == null
                ? new ArrayList<>()
                : roleList.stream()
                        .map(role -> EntityIdCodec.toValue(role.getId()))
                        .collect(Collectors.toList());
    }

    public boolean hasRole(@NotNull Role target) {
        return getRoleIdList().stream()
                .anyMatch(roleId -> StringUtils.equals(roleId, EntityIdCodec.toValue(target.getId())));
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

    @Override
    public String getSignName() {
        return BEAN_NAME;
    }

    @Override
    public String getSignBody() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("departmentId", this.getDepartmentId());
        map.put("loginName", this.getLoginName());
        map.put("loginPass", this.getLoginPass());
        map.put("email", this.getEmail());
        map.put("mobile", this.getMobile());
        map.put("name", this.getName());
        map.put("ranks", this.getRanks());

        map.put("super", this.isSuper());
        map.put("admin", this.isAdmin());
        map.put("enable", this.isEnable());

        map.put("lastLoginDate", this.getLastLoginDate());
        map.put("lastLoginIp", this.getLastLoginIp());

        return JsonUtils.toJson(map);
    }
}
