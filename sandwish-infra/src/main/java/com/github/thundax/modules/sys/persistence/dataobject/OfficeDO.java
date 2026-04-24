package com.github.thundax.modules.sys.persistence.dataobject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.thundax.common.persistence.AdminTreeEntity;

import java.io.Serializable;

/**
 * 机构持久化对象。
 */
public class OfficeDO extends AdminTreeEntity<OfficeDO> {

    private static final long serialVersionUID = 1L;

    private String parentId;
    private Integer lft;
    private Integer rgt;
    private String name;
    private String shortName;

    public OfficeDO() {
        super();
    }

    public OfficeDO(String id) {
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

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public static class Query implements Serializable {

        private static final long serialVersionUID = 1L;

        private String parentId;
        private String name;
        private String remarks;

        public String getParentId() {
            return parentId;
        }

        public void setParentId(String parentId) {
            this.parentId = parentId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRemarks() {
            return remarks;
        }

        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }
    }
}
