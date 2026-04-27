package com.github.thundax.modules.sys.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.common.config.Global;
import com.github.thundax.common.utils.JsonUtils;
import com.github.thundax.common.utils.StringUtils;
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

/** @author wdit */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Role extends BaseRole {

    public static final String BEAN_NAME = "Role";

    private List<String> menuIdList;

    public Role() {
        super();
    }

    public Role(String id) {
        super(id);
    }

    public Role toBean() {
        return RoleServiceHolder.get(this.getId());
    }

    @JsonIgnore
    public boolean isAdmin() {
        return Global.YES.equals(this.getAdminFlag());
    }

    @JsonIgnore
    public boolean isEnable() {
        return Global.YES.equals(this.getEnableFlag());
    }

    public List<String> getMenuIdList() {
        if (this.menuIdList == null) {
            if (this.getIsNewRecord()) {
                this.menuIdList = new ArrayList<>();
            } else {
                this.menuIdList =
                        ListUtils.map(
                                RoleServiceHolder.getService().findRoleMenu(this), Menu::getId);
            }
        }
        return this.menuIdList;
    }

    public void setMenuIdList(List<String> menuIdList) {
        this.menuIdList = menuIdList;
    }

    @JsonIgnore
    public List<Menu> getMenuList() {
        return ListUtils.map(getMenuIdList(), MenuServiceHolder::get);
    }

    public void setMenuList(List<Menu> menuList) {
        this.menuIdList = ListUtils.map(menuList, Menu::getId);
    }

    @JsonIgnore
    public List<User> getUserList() {
        return RoleServiceHolder.getService().findRoleUser(this);
    }

    /** 获取全部权限字符串 */
    @JsonIgnore
    public Set<String> getPerms() {
        Set<String> allPerms = new HashSet<>();
        for (Menu menu : this.getMenuList()) {
            allPerms.addAll(menu.getAllPerms());
        }
        return allPerms;
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
        map.put("name", this.getName());
        map.put("admin", this.isAdmin());
        map.put("enable", this.isEnable());

        List<String> menuIds = new ArrayList<>(this.getMenuIdList());
        menuIds.sort(StringUtils::compare);
        map.put("menus", menuIds);

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
