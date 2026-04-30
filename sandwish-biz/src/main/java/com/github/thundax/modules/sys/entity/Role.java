package com.github.thundax.modules.sys.entity;

import com.github.thundax.common.config.Global;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.utils.JsonUtils;
import com.github.thundax.modules.sys.entity.base.BaseRole;
import com.github.thundax.modules.sys.utils.MenuServiceHolder;
import com.github.thundax.modules.sys.utils.RoleServiceHolder;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class Role extends BaseRole {

    public static final String BEAN_NAME = "Role";

    private List<String> menuIdList;

    public Role toBean() {
        return RoleServiceHolder.get(this.getId());
    }

    public boolean isAdmin() {
        return Global.YES.equals(this.getAdminFlag());
    }

    public boolean isEnable() {
        return Global.YES.equals(this.getEnableFlag());
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

        public static final String PROP_ENABLE_FLAG = "enableFlag";

        private String enableFlag;

        public String getEnableFlag() {
            return enableFlag;
        }

        public void setEnableFlag(String enableFlag) {
            this.enableFlag = enableFlag;
        }
    }
}
