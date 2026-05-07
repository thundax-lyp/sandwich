package com.github.thundax.modules.sys.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.domain.Sortable;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.utils.JsonUtils;
import com.github.thundax.modules.sys.entity.enums.MenuVisibility;
import com.github.thundax.modules.sys.entity.valueobject.AccessRank;
import com.github.thundax.modules.sys.entity.valueobject.PermissionCode;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Menu implements Auditable, Sortable, Comparable<Menu> {
    private EntityId id;

    private EntityId parentId;

    private String name;
    private String perms;
    private AccessRank rank = AccessRank.of(0);
    private MenuVisibility visibility = MenuVisibility.VISIBLE;
    private String displayParams;
    private String url;
    private String target;
    private int priority;
    private String remarks;
    private Date createDate;
    private Date updateDate;
    private String createUserId;
    private String updateUserId;

    public void setParentId(String parentId) {
        this.parentId = StringUtils.isBlank(parentId) ? null : EntityIdCodec.toDomain(parentId);
    }

    public void setParentId(Long parentId) {
        this.parentId = EntityIdCodec.toDomain(parentId);
    }

    public void setParent(Menu parent) {
        this.parentId = parent == null ? null : parent.getId();
    }

    public Set<String> getAllPerms() {
        Set<String> allPerms = Sets.newHashSet();
        if (StringUtils.isNotBlank(this.getPerms())) {
            for (String perm : this.getPerms().split(PermissionCode.SEPARATOR)) {
                if (StringUtils.isNotBlank(perm)) {
                    allPerms.add(perm.trim());
                }
            }
        }
        return allPerms;
    }

    public boolean isDisplay() {
        return MenuVisibility.VISIBLE == getVisibility();
    }

    public Object getDisplayParam(String paramName, Object defaultValue) {
        Map<String, Object> map = getDisplayParamMap();
        if (!map.containsKey(paramName)) {
            return defaultValue;
        }
        return map.get(paramName);
    }

    public Map<String, Object> getDisplayParamMap() {
        String displayParams = this.getDisplayParams();
        if (StringUtils.isEmpty(displayParams)) {
            return Maps.newHashMap();
        }
        return JsonUtils.fromJson(displayParams, new TypeReference<Map<String, Object>>() {});
    }

    @Override
    public int compareTo(Menu that) {
        int priorityCompare = compareInteger(this.getPriority(), that.getPriority());
        if (priorityCompare != 0) {
            return priorityCompare;
        }
        int rankCompare = this.getRank().compareTo(that.getRank());
        if (rankCompare != 0) {
            return rankCompare;
        }
        return StringUtils.compare(this.getName(), that.getName());
    }

    private static int compareInteger(Integer left, Integer right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return -1;
        }
        if (right == null) {
            return 1;
        }
        return left.compareTo(right);
    }

    public AccessRank getRank() {
        return rank == null ? AccessRank.of(null) : rank;
    }

    public void setRank(AccessRank rank) {
        this.rank = rank == null ? AccessRank.of(null) : rank;
    }
}
