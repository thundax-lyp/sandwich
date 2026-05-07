package com.github.thundax.modules.sys.entity;

import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.domain.Sortable;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.entity.enums.MenuVisibility;
import com.github.thundax.modules.sys.entity.valueobject.AccessRank;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    public boolean isDisplay() {
        return MenuVisibility.VISIBLE == getVisibility();
    }

    @Override
    public int compareTo(Menu that) {
        return compareInteger(this.getPriority(), that.getPriority());
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
