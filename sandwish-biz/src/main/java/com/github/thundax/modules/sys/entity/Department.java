package com.github.thundax.modules.sys.entity;

import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.domain.Sortable;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
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
public class Department implements Auditable, Sortable {
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

    public static final String BEAN_NAME = "department";

    public void setParent(Department parent) {
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
}
