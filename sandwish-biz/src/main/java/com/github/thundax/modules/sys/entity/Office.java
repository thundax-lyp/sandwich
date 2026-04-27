package com.github.thundax.modules.sys.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.modules.sys.entity.base.BaseOffice;
import com.github.thundax.modules.sys.utils.OfficeServiceHolder;
import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.List;

/** @author wdit */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Office extends BaseOffice {

    public static final String BEAN_NAME = "Office";

    public Office() {
        super();
    }

    public Office(String id) {
        super(id);
    }

    @JsonIgnore
    public Office toBean() {
        return OfficeServiceHolder.get(this.getId());
    }

    @JsonIgnore
    public Office getParent() {
        return OfficeServiceHolder.get(this.getParentId());
    }

    public void setParent(Office parent) {
        this.setParentId(parent == null ? null : parent.getId());
    }

    /**
     * 获取名称路径
     *
     * @return /一级部门/二级部门/....
     */
    @JsonIgnore
    public String getNamePath() {
        List<String> nameList = Lists.newArrayList();
        Office node = this;
        while (node != null && node.getId() != null) {
            node = OfficeServiceHolder.getService().get(node.getId());
            if (node != null) {
                nameList.add(0, node.getName());
                node = node.getParent();
            }
        }
        return StringUtils.join(nameList, "/");
    }

    /**
     * 获取显示名称。如果存在简称，则显示简称；如果没有简称，则显示全名
     *
     * @return 显示名称
     */
    @JsonIgnore
    public String getDisplayName() {
        if (StringUtils.isNotBlank(this.getShortName())) {
            return this.getShortName();
        }
        return this.getName();
    }

    /**
     * 获取显示名的路径
     *
     * @return 路径
     */
    @JsonIgnore
    public String getDisplayNamePath() {
        List<String> nameList = Lists.newArrayList();
        Office node = this;
        while (node != null && node.getId() != null) {
            node = OfficeServiceHolder.getService().get(node.getId());
            if (node != null) {
                nameList.add(0, node.getDisplayName());
                node = node.getParent();
            }
        }
        return StringUtils.join(nameList, "/");
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

        public static final String PROP_PARENT_ID = "parentId";
        public static final String PROP_NAME = "name";
        public static final String PROP_REMARKS = "remarks";

        private String parentId; // 根据parentId过滤。
        // 0: parent_id IS NULL;
        // 其他: parent_id = parentId
        private String name; // 根据name/shortName/enname过滤
        private String remarks; // 根据remarks过滤

        // 根据parentId过滤
        public String getParentId() {
            return this.parentId;
        }

        public void setParentId(String parentId) {
            this.parentId = parentId;
        }

        // 根据name过滤
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        // 根据remarks过滤
        public String getRemarks() {
            return remarks;
        }

        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }
    }
}
