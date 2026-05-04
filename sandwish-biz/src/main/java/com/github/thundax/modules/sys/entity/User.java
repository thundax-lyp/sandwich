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
 *
 * <p>登录标识和认证凭据迁移到 auth 模型后，当前类只保留用户资料、组织关系、权限等级和状态语义。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User implements Auditable, Signable, Sortable {
    private EntityId id;

    private String officeId;

    /**
     * 迁移兼容字段，目标登录标识固定由 UserIdentity.identityValue 承载。
     */
    @Deprecated
    private String loginName;

    /**
     * 迁移兼容字段，目标密码凭据固定由 UserCredential.credentialValue 承载。
     */
    @Deprecated
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

    public void setOffice(Office office) {
        this.setOfficeId(office == null ? null : EntityIdCodec.toValue(office.getId()));
    }

    public boolean isBelongTo(Office office) {
        return office != null && Objects.equals(this.getOfficeId(), EntityIdCodec.toValue(office.getId()));
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
        map.put("officeId", this.getOfficeId());
        // 迁移期仍保留旧字段签名输入，避免用户保存链路行为变化。
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
