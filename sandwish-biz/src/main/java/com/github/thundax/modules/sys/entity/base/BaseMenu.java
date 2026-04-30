package com.github.thundax.modules.sys.entity.base;

import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.domain.Entity;
import com.github.thundax.common.domain.Signable;
import com.github.thundax.common.domain.Sortable;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.sys.entity.Menu;
import java.util.Date;
import org.springframework.lang.NonNull;

public abstract class BaseMenu extends Entity<Menu> implements Auditable, Signable, Sortable {

    public static final String ROOT_ID = "ROOT";

    private String parentId;

    private String name;
    private String perms;
    private Integer ranks;
    private String displayFlag;
    private String displayParams;
    private String url;
    private String target;
    private int priority;
    private String remarks;
    private Date createDate;
    private Date updateDate;
    private String createUserId;
    private String updateUserId;

    public BaseMenu() {
        initialize();
    }

    protected void initialize() {}

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

    @Override
    @NonNull
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority >= 0 ? priority : 0;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    @Override
    public String getCreateUserId() {
        return createUserId;
    }

    @Override
    public void setCreateUserId(String createUserId) {
        this.createUserId = createUserId;
    }

    @Override
    public String getUpdateUserId() {
        return updateUserId;
    }

    @Override
    public void setUpdateUserId(String updateUserId) {
        this.updateUserId = updateUserId;
    }

    @Override
    public String getSignId() {
        return EntityIdCodec.toValue(getId());
    }
}
