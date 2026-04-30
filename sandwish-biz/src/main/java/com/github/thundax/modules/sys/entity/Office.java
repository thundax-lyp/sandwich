package com.github.thundax.modules.sys.entity;

import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.domain.Sortable;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Office implements Auditable, Sortable {
    private EntityId id;

    public static final String ROOT_ID = "ROOT";

    private String parentId;

    private String name;
    private String shortName;
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

    public static final String BEAN_NAME = "Office";

    public void setParent(Office parent) {
        this.setParentId(parent == null ? null : EntityIdCodec.toValue(parent.getId()));
    }

    /**
     * 获取显示名称。如果存在简称，则显示简称；如果没有简称，则显示全名
     *
     * @return 显示名称
     */
    public String getDisplayName() {
        if (StringUtils.isNotBlank(this.getShortName())) {
            return this.getShortName();
        }
        return this.getName();
    }

    private Query query;

    public Query getQuery() {
        return this.query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public static class Query implements Serializable {

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
