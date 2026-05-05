package com.github.thundax.modules.sys.entity;

import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.domain.Signable;
import com.github.thundax.common.domain.Sortable;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.utils.JsonUtils;
import com.github.thundax.modules.sys.entity.enums.UserPrivilege;
import com.github.thundax.modules.sys.entity.enums.UserStatus;
import com.github.thundax.modules.sys.entity.valueobject.UserRank;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
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
public class User implements Auditable, Signable, Sortable {
    public static final String BEAN_NAME = "User";

    private EntityId id;

    private String departmentId;

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

    @NonNull
    public Integer getRanks() {
        return rank().value();
    }

    @NonNull
    public UserRank rank() {
        return UserRank.of(this.ranks);
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
