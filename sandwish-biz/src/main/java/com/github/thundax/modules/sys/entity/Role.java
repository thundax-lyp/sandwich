package com.github.thundax.modules.sys.entity;

import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.domain.Signable;
import com.github.thundax.common.domain.Sortable;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.utils.JsonUtils;
import com.github.thundax.modules.sys.utils.MenuServiceHolder;
import com.github.thundax.modules.sys.utils.RoleServiceHolder;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role implements Auditable, Signable, Sortable {
    private EntityId id;
    private String name;
    private RolePrivilege privilege = RolePrivilege.NORMAL;
    private RoleStatus status;
    private int priority;
    private String remarks;
    private Date createDate;
    private Date updateDate;
    private String createUserId;
    private String updateUserId;

    @Override
    public void setPriority(int priority) {
        this.priority = priority >= 0 ? priority : 0;
    }

    @Override
    public String getSignId() {
        return EntityIdCodec.toValue(getId());
    }

    public static final String BEAN_NAME = "Role";

    private List<String> menuIdList;

    public Role toBean() {
        return RoleServiceHolder.get(this.getId());
    }

    public boolean isAdmin() {
        return RolePrivilege.ADMIN == getPrivilege();
    }

    public boolean isEnable() {
        return RoleStatus.ENABLED == getStatus();
    }

    public List<String> getMenuIdList() {
        if (this.menuIdList == null) {
            if (StringUtils.isBlank(EntityIdCodec.toValue(getId()))) {
                this.menuIdList = new ArrayList<>();
            } else {
                this.menuIdList = RoleServiceHolder.getService().findRoleMenu(this).stream()
                        .map(menu -> EntityIdCodec.toValue(menu.getId()))
                        .collect(Collectors.toList());
            }
        }
        return this.menuIdList;
    }

    public void setMenuIdList(List<String> menuIdList) {
        this.menuIdList = menuIdList;
    }

    public List<Menu> getMenuList() {
        return getMenuIdList().stream()
                .map(menuId -> MenuServiceHolder.get(EntityIdCodec.toDomain(menuId)))
                .collect(Collectors.toList());
    }

    public void setMenuList(List<Menu> menuList) {
        this.menuIdList = menuList == null
                ? new ArrayList<>()
                : menuList.stream()
                        .map(menu -> EntityIdCodec.toValue(menu.getId()))
                        .collect(Collectors.toList());
    }

    public List<User> getUserList() {
        return RoleServiceHolder.getService().findRoleUser(this);
    }

    public Set<String> getPerms() {
        Set<String> allPerms = new HashSet<>();
        for (Menu menu : this.getMenuList()) {
            allPerms.addAll(menu.getAllPerms());
        }
        return allPerms;
    }

    @Override
    public String getSignName() {
        return BEAN_NAME;
    }

    @Override
    public String getSignBody() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", this.getName());
        map.put("admin", this.isAdmin());
        map.put("enable", this.isEnable());

        List<String> menuIds = new ArrayList<>(this.getMenuIdList());
        menuIds.sort(StringUtils::compare);
        map.put("menus", menuIds);

        return JsonUtils.toJson(map);
    }

    private Query query;

    public Query getQuery() {
        return this.query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public static class Query implements Serializable {

        public static final String PROP_STATUS = "status";

        private RoleStatus status;

        public RoleStatus getStatus() {
            return status;
        }

        public void setStatus(RoleStatus status) {
            this.status = status;
        }

        public void setStatus(String status) {
            this.status = StringUtils.isBlank(status) ? null : RoleStatus.from(status);
        }
    }
}
