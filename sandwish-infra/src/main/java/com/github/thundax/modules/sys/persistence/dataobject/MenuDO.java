package com.github.thundax.modules.sys.persistence.dataobject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.thundax.common.persistence.AdminTreeEntity;

import java.io.Serializable;

/**
 * 菜单持久化对象。
 */
public class MenuDO extends AdminTreeEntity<MenuDO> {

    private static final long serialVersionUID = 1L;

    private String parentId;
    private Integer lft;
    private Integer rgt;
    private String name;
    private String perms;
    private Integer ranks;
    private String displayFlag;
    private String displayParams;
    private String url;
    private String target;

    public MenuDO() {
        super();
    }

    public MenuDO(String id) {
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

    @Override
    public String getParentId() {
        return parentId;
    }

    @Override
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @Override
    public Integer getLft() {
        return lft;
    }

    @Override
    public void setLft(Integer lft) {
        this.lft = lft;
    }

    @Override
    public Integer getRgt() {
        return rgt;
    }

    @Override
    public void setRgt(Integer rgt) {
        this.rgt = rgt;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPerms() {
        return perms;
    }

    public void setPerms(String perms) {
        this.perms = perms;
    }

    public Integer getRanks() {
        return ranks;
    }

    public void setRanks(Integer ranks) {
        this.ranks = ranks;
    }

    public String getDisplayFlag() {
        return displayFlag;
    }

    public void setDisplayFlag(String displayFlag) {
        this.displayFlag = displayFlag;
    }

    public String getDisplayParams() {
        return displayParams;
    }

    public void setDisplayParams(String displayParams) {
        this.displayParams = displayParams;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public static class Query implements Serializable {

        private static final long serialVersionUID = 1L;

        private String parentId;
        private String displayFlag;
        private Integer maxRank;

        public String getParentId() {
            return parentId;
        }

        public void setParentId(String parentId) {
            this.parentId = parentId;
        }

        public String getDisplayFlag() {
            return displayFlag;
        }

        public void setDisplayFlag(String displayFlag) {
            this.displayFlag = displayFlag;
        }

        public Integer getMaxRank() {
            return maxRank;
        }

        public void setMaxRank(Integer maxRank) {
            this.maxRank = maxRank;
        }
    }
}
