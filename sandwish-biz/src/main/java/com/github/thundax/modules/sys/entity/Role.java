package com.github.thundax.modules.sys.entity;

import com.github.thundax.common.domain.Sortable;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.sys.entity.enums.RolePrivilege;
import com.github.thundax.modules.sys.entity.enums.RoleStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role implements Sortable {
    public static final String BEAN_NAME = "Role";

    private EntityId id;
    private String name;
    private RolePrivilege privilege = RolePrivilege.NORMAL;
    private RoleStatus status;
    private int priority;
    private String remarks;

    private List<Long> menuIdList;

    public boolean isAdmin() {
        return RolePrivilege.ADMIN == getPrivilege();
    }

    public boolean isEnable() {
        return RoleStatus.ENABLED == getStatus();
    }

    public List<Long> getMenuIdList() {
        if (this.menuIdList == null) {
            this.menuIdList = new ArrayList<>();
        }
        return this.menuIdList;
    }

    public void setMenuIdList(List<Long> menuIdList) {
        this.menuIdList = menuIdList;
    }

    public void setMenuList(List<Menu> menuList) {
        this.menuIdList = menuList == null
                ? new ArrayList<>()
                : menuList.stream()
                        .map(menu -> EntityIdCodec.toValue(menu.getId()))
                        .collect(Collectors.toList());
    }
}
