package com.github.thundax.modules.sys.persistence.dataobject;

import com.github.thundax.common.collect.ListUtils;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 角色持久化对象。
 */
@NoArgsConstructor
public class RoleDO {

    public static final String DEL_FLAG_NORMAL = "0";

    private String id;
    private boolean isNewRecord;
    private String name;
    private String adminFlag;
    private String enableFlag;
    private List<String> menuIdList;
    private Integer priority;
    private String remarks;
    private Date createDate;
    private String createUserId;
    private Date updateDate;
    private String updateUserId;
    private String delFlag;
    private String queryEnableFlag;

    public RoleDO(String id) {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdminFlag() {
        return adminFlag;
    }

    public void setAdminFlag(String adminFlag) {
        this.adminFlag = adminFlag;
    }

    public String getEnableFlag() {
        return enableFlag;
    }

    public void setEnableFlag(String enableFlag) {
        this.enableFlag = enableFlag;
    }

    public List<String> getMenuIdList() {
        if (menuIdList == null) {
            menuIdList = ListUtils.newArrayList();
        }
        return menuIdList;
    }

    public void setMenuIdList(List<String> menuIdList) {
        this.menuIdList = menuIdList;
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

    public String getQueryEnableFlag() {
        return queryEnableFlag;
    }

    public void setQueryEnableFlag(String queryEnableFlag) {
        this.queryEnableFlag = queryEnableFlag;
    }
}
