package com.github.thundax.modules.sys.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.thundax.common.config.Global;
import com.github.thundax.common.utils.JsonUtils;
import com.github.thundax.modules.sys.entity.base.BaseMenu;
import com.github.thundax.modules.sys.utils.MenuServiceHolder;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Menu extends BaseMenu implements Comparable<Menu> {

    public static final String BEAN_NAME = "Menu";

    public static final String PERM_SEPARATOR = ",";
    public static final String PERM_USER = "user";
    public static final String PERM_ADMIN = "admin";
    public static final String PERM_SUPER = "super";

    public Menu() {
        super();
    }

    public Menu(String id) {
        super(id);
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.setDisplayFlag(Global.SHOW);
    }

    public Menu toBean() {
        return MenuServiceHolder.get(getId());
    }

    @Override
    public void setParentId(String parentId) {
        super.setParentId(StringUtils.isBlank(parentId) ? null : parentId);
    }

    @JsonIgnore
    public Menu getParent() {
        return MenuServiceHolder.get(this.getParentId());
    }

    public void setParent(Menu parent) {
        this.setParentId(parent == null ? null : parent.getId());
    }

    @JsonIgnore
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

    @JsonIgnore
    public boolean isDisplay() {
        return Global.SHOW.equals(this.getDisplayFlag());
    }

    public Object getDisplayParam(String paramName, Object defaultValue) {
        Map<String, Object> map = getDisplayParamMap();
        if (!map.containsKey(paramName)) {
            return defaultValue;
        }
        return map.get(paramName);
    }

    @JsonIgnore
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
    @JsonIgnore
    public String getSignName() {
        return BEAN_NAME;
    }

    @Override
    @JsonIgnore
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
        public static final String PROP_DISPLAY_FLAG = "displayFlag";
        public static final String PROP_MAX_RANK = "maxRank";

        private String parentId;
        private String displayFlag;
        private Integer maxRank; // 按照rank查询

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
            return this.maxRank;
        }

        public void setMaxRank(Integer maxRank) {
            this.maxRank = maxRank;
        }
    }
}
