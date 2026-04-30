package com.github.thundax.modules.sys.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.domain.Signable;
import com.github.thundax.common.domain.Sortable;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.utils.JsonUtils;
import com.github.thundax.modules.sys.utils.MenuServiceHolder;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
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
public class Menu implements Auditable, Signable, Sortable, Comparable<Menu> {
    private EntityId id;

    public static final String ROOT_ID = "ROOT";

    private String parentId;

    private String name;
    private String perms;
    private Integer ranks;
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

    @Override
    public void setPriority(int priority) {
        this.priority = priority >= 0 ? priority : 0;
    }

    @Override
    public String getSignId() {
        return EntityIdCodec.toValue(getId());
    }

    public static final String BEAN_NAME = "Menu";

    public static final String PERM_SEPARATOR = ",";
    public static final String PERM_USER = "user";
    public static final String PERM_ADMIN = "admin";
    public static final String PERM_SUPER = "super";

    public Menu toBean() {
        return MenuServiceHolder.get(getId());
    }

    public void setParentId(String parentId) {
        this.parentId = StringUtils.isBlank(parentId) ? null : parentId;
    }

    public Menu getParent() {
        return MenuServiceHolder.get(EntityIdCodec.toDomain(this.getParentId()));
    }

    public void setParent(Menu parent) {
        this.setParentId(parent == null ? null : EntityIdCodec.toValue(parent.getId()));
    }

    public Set<String> getAllPerms() {
        Set<String> allPerms = Sets.newHashSet();
        if (StringUtils.isNotBlank(this.getPerms())) {
            for (String perm : this.getPerms().split(PERM_SEPARATOR)) {
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
        int ranksCompare = compareInteger(this.getRanks(), that.getRanks());
        if (ranksCompare != 0) {
            return ranksCompare;
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

    @Override
    public String getSignName() {
        return BEAN_NAME;
    }

    @Override
    public String getSignBody() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", this.getName());
        map.put("parentId", this.getParentId());
        map.put("perms", this.getPerms());
        map.put("ranks", this.getRanks());
        map.put("display", this.isDisplay());
        map.put("url", this.getUrl());
        map.put("target", this.getTarget());

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

        public static final String PROP_PARENT_ID = "parentId";
        public static final String PROP_VISIBILITY = "visibility";
        public static final String PROP_MAX_RANK = "maxRank";

        private String parentId;
        private MenuVisibility visibility;
        private Integer maxRank; // 按照rank查询

        public String getParentId() {
            return parentId;
        }

        public void setParentId(String parentId) {
            this.parentId = parentId;
        }

        public MenuVisibility getVisibility() {
            return visibility;
        }

        public void setVisibility(MenuVisibility visibility) {
            this.visibility = visibility;
        }

        public void setVisibility(String visibility) {
            this.visibility = StringUtils.isBlank(visibility) ? null : MenuVisibility.from(visibility);
        }

        public Integer getMaxRank() {
            return this.maxRank;
        }

        public void setMaxRank(Integer maxRank) {
            this.maxRank = maxRank;
        }
    }
}
