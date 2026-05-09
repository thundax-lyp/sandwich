package com.github.thundax.modules.sys.entity;

import com.github.thundax.common.domain.Sortable;
import com.github.thundax.modules.sys.entity.enums.MenuVisibility;
import com.github.thundax.modules.sys.entity.valueobject.AccessRank;
import com.github.thundax.modules.sys.entity.valueobject.MenuId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Menu implements Sortable, Comparable<Menu> {
    private MenuId id;

    private MenuId parentId;

    private String name;
    private String perms;
    private AccessRank rank = AccessRank.of(0);
    private MenuVisibility visibility = MenuVisibility.VISIBLE;
    private String displayParams;
    private String url;
    private String target;
    private int priority;
    private String remarks;

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
