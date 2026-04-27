package com.github.thundax.modules.sys.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 菜单持久化对象。 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_menu")
public class MenuDO {

    public static final String DEL_FLAG_NORMAL = "0";
    public static final String ROOT_ID = "ROOT";

    private String id;
    private boolean isNewRecord;
    private String parentId;
    private Integer lft;
    private Integer rgt;
    private String name;
    private String perms;
    private Integer ranks;
    private String displayFlag;
    private String displayParams;
    private String url;
    private String target;
    private Integer priority;
    private String remarks;
    private Date createDate;
    private String createUserId;
    private Date updateDate;
    private String updateUserId;
    private String delFlag;
    private String queryParentId;
    private String queryDisplayFlag;
    private Integer queryMaxRank;

    public MenuDO(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean getIsNewRecord() {
        return isNewRecord;
    }

    public void setIsNewRecord(boolean isNewRecord) {
        this.isNewRecord = isNewRecord;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Integer getLft() {
        return lft;
    }

    public void setLft(Integer lft) {
        this.lft = lft;
    }

    public Integer getRgt() {
        return rgt;
    }

    public void setRgt(Integer rgt) {
        this.rgt = rgt;
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

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
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

    public String getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(String createUserId) {
        this.createUserId = createUserId;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getUpdateUserId() {
        return updateUserId;
    }

    public void setUpdateUserId(String updateUserId) {
        this.updateUserId = updateUserId;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getQueryParentId() {
        return queryParentId;
    }

    public void setQueryParentId(String queryParentId) {
        this.queryParentId = queryParentId;
    }

    public String getQueryDisplayFlag() {
        return queryDisplayFlag;
    }

    public void setQueryDisplayFlag(String queryDisplayFlag) {
        this.queryDisplayFlag = queryDisplayFlag;
    }

    public Integer getQueryMaxRank() {
        return queryMaxRank;
    }

    public void setQueryMaxRank(Integer queryMaxRank) {
        this.queryMaxRank = queryMaxRank;
    }

    public Integer treeSpan() {
        return this.rgt - this.lft + 1;
    }
}
