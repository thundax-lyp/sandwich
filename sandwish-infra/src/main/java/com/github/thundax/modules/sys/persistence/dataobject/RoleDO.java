package com.github.thundax.modules.sys.persistence.dataobject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.common.persistence.AdminDataEntity;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 角色持久化对象。
 */
@NoArgsConstructor
public class RoleDO extends AdminDataEntity<RoleDO> {

    private String name;
    private String adminFlag;
    private String enableFlag;
    private List<String> menuIdList;

    public RoleDO(String id) {
        super(id);
    }

    @Override
    protected Object createQueryObject() {
        return new Query();
    }

    @JsonIgnore
    public Query getQuery() {
        return (Query) this.query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdminFlag() {
        return adminFlag;
    }

    public void setAdminFlag(String adminFlag) {
        this.adminFlag = adminFlag;
    }

    public String getEnableFlag() {
        return enableFlag;
    }

    public void setEnableFlag(String enableFlag) {
        this.enableFlag = enableFlag;
    }

    public List<String> getMenuIdList() {
        if (menuIdList == null) {
            menuIdList = ListUtils.newArrayList();
        }
        return menuIdList;
    }

    public void setMenuIdList(List<String> menuIdList) {
        this.menuIdList = menuIdList;
    }

    public static class Query implements Serializable {

        private String enableFlag;

        public String getEnableFlag() {
            return enableFlag;
        }

        public void setEnableFlag(String enableFlag) {
            this.enableFlag = enableFlag;
        }
    }
}
