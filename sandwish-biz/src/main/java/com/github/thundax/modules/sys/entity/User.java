package com.github.thundax.modules.sys.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.thundax.common.config.Global;
import com.github.thundax.common.utils.JsonUtils;
import com.github.thundax.modules.sys.entity.base.BaseUser;
import com.github.thundax.modules.sys.utils.OfficeServiceHolder;
import com.github.thundax.modules.sys.utils.RoleServiceHolder;
import com.github.thundax.modules.sys.utils.UserServiceHolder;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class User extends BaseUser {

    public static final String BEAN_NAME = "User";

    public static final int MAX_RANKS = 9;

    private List<String> roleIdList;

    public User() {
        super();
    }

    public User(String id) {
        super(id);
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.setLoginCount(0);
        this.setRanks(0);
        this.setSuperFlag(Global.NO);
        this.setAdminFlag(Global.NO);
    }

    @Override
    @NonNull
    public Integer getRanks() {
        Integer ranks = super.getRanks();
        if (ranks == null || ranks < 0) {
            return 0;
        } else if (ranks >= MAX_RANKS) {
            return MAX_RANKS;
        } else {
            return ranks;
        }
    }

    @JsonIgnore
    public Office getOffice() {
        return OfficeServiceHolder.getService().get(this.getOfficeId());
    }

    public void setOffice(Office office) {
        this.setOfficeId(office == null ? null : office.getId());
    }

    public boolean isBelongTo(Office office) {
        return office != null && Objects.equals(this.getOfficeId(), office.getId());
    }

    @NotNull
    public List<String> getRoleIdList() {
        if (this.roleIdList == null) {
            if (StringUtils.isBlank(this.getId())) {
                this.roleIdList = new ArrayList<>();
            } else {
                this.roleIdList = UserServiceHolder.getService().findUserRole(this).stream()
                        .map(role -> role.getId())
                        .collect(Collectors.toList());
            }
        }
        return this.roleIdList;
    }

    public void setRoleIdList(List<String> roleIdList) {
        this.roleIdList = roleIdList;
    }

    @JsonIgnore
    @NotNull
    public List<Role> getRoleList() {
        return getRoleIdList().stream()
                .map(roleId -> RoleServiceHolder.get(roleId))
                .collect(Collectors.toList());
    }

    public void setRoleList(List<Role> roleList) {
        this.roleIdList = roleList == null
                ? new ArrayList<>()
                : roleList.stream().map(role -> role.getId()).collect(Collectors.toList());
    }

    @JsonIgnore
    public boolean hasRole(@NotNull Role target) {
        return getRoleIdList().stream().anyMatch(roleId -> StringUtils.equals(roleId, target.getId()));
    }

    @JsonIgnore
    public boolean isSuper() {
        return Global.YES.equals(this.getSuperFlag());
    }

    @JsonIgnore
    public boolean isAdmin() {
        return Global.YES.equals(this.getAdminFlag());
    }

    @JsonIgnore
    public boolean isEnable() {
        return Global.YES.equals(this.getEnableFlag());
    }

    @Override
    @JsonIgnore
    public String getSignName() {
        return BEAN_NAME;
    }

    @Override
    @JsonIgnore
    public String getSignBody() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("officeId", this.getOfficeId());
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

    private Query query;

    @JsonIgnore
    public Query getQuery() {
        return this.query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public static class Query implements Serializable {

        private static final long serialVersionUID = 1L;

        public static final String PROP_OFFICE_ID = "officeId";
        public static final String PROP_LOGIN_NAME = "loginName";
        public static final String PROP_NAME = "name";
        public static final String PROP_ENABLE_FLAG = "enableFlag";
        public static final String PROP_SUPER_FLAG = "superFlag";
        public static final String PROP_ORDER_BY = "orderBy";

        private String officeId; // 按照机构查询
        private String loginName; // 按照登录名查询
        private String name; // 按照姓名名查询
        private String enableFlag; // 按照enableFlag查询
        private String superFlag; // 按照superFlag查询

        private String orderBy;

        public String getOfficeId() {
            return this.officeId;
        }

        public void setOfficeId(String officeId) {
            this.officeId = officeId;
        }

        // a.login_name LIKE '%'+#{query.loginName}+'%'
        public String getLoginName() {
            return this.loginName;
        }

        public void setLoginName(String loginName) {
            this.loginName = loginName;
        }

        // a.name LIKE '%'+#{query.name}+'%'
        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        // a.enable_flag = #{query.enableFlag}
        public String getEnableFlag() {
            return this.enableFlag;
        }

        public void setEnableFlag(String enableFlag) {
            this.enableFlag = enableFlag;
        }

        // a.super_flag = #{query.superFlag}
        public String getSuperFlag() {
            return this.superFlag;
        }

        public void setSuperFlag(String superFlag) {
            this.superFlag = superFlag;
        }

        public String getOrderBy() {
            return orderBy;
        }

        public void setOrderBy(String orderBy) {
            this.orderBy = orderBy;
        }
    }
}
