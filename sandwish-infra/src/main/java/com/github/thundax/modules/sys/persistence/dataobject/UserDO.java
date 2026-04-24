package com.github.thundax.modules.sys.persistence.dataobject;

import com.github.thundax.common.collect.ListUtils;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 用户持久化对象。
 */
@NoArgsConstructor
public class UserDO {

    public static final String DEL_FLAG_NORMAL = "0";

    private String id;
    private boolean isNewRecord;

    private String officeId;
    private String loginName;
    private String loginPass;
    private String email;
    private String mobile;
    private String tel;
    private String name;
    private Integer ranks;
    private Date registerDate;
    private String registerIp;
    private Date lastLoginDate;
    private String lastLoginIp;
    private Integer loginCount;
    private String superFlag;
    private String adminFlag;
    private String enableFlag;
    private String ssoLoginName;
    private List<String> roleIdList;
    private Integer priority;
    private String remarks;
    private Date createDate;
    private String createUserId;
    private Date updateDate;
    private String updateUserId;
    private String delFlag;
    private String queryOfficeId;
    private Integer queryOfficeTreeLeft;
    private Integer queryOfficeTreeRight;
    private String queryLoginName;
    private String queryName;
    private String queryEnableFlag;
    private String querySuperFlag;
    private String queryOrderBy;

    public UserDO(String id) {
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

    public String getOfficeId() {
        return officeId;
    }

    public void setOfficeId(String officeId) {
        this.officeId = officeId;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getLoginPass() {
        return loginPass;
    }

    public void setLoginPass(String loginPass) {
        this.loginPass = loginPass;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getRanks() {
        return ranks;
    }

    public void setRanks(Integer ranks) {
        this.ranks = ranks;
    }

    public Date getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(Date registerDate) {
        this.registerDate = registerDate;
    }

    public String getRegisterIp() {
        return registerIp;
    }

    public void setRegisterIp(String registerIp) {
        this.registerIp = registerIp;
    }

    public Date getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public Integer getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(Integer loginCount) {
        this.loginCount = loginCount;
    }

    public String getSuperFlag() {
        return superFlag;
    }

    public void setSuperFlag(String superFlag) {
        this.superFlag = superFlag;
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

    public String getSsoLoginName() {
        return ssoLoginName;
    }

    public void setSsoLoginName(String ssoLoginName) {
        this.ssoLoginName = ssoLoginName;
    }

    public List<String> getRoleIdList() {
        if (roleIdList == null) {
            roleIdList = ListUtils.newArrayList();
        }
        return roleIdList;
    }

    public void setRoleIdList(List<String> roleIdList) {
        this.roleIdList = roleIdList;
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

    public String getQueryOfficeId() {
        return queryOfficeId;
    }

    public void setQueryOfficeId(String queryOfficeId) {
        this.queryOfficeId = queryOfficeId;
    }

    public Integer getQueryOfficeTreeLeft() {
        return queryOfficeTreeLeft;
    }

    public void setQueryOfficeTreeLeft(Integer queryOfficeTreeLeft) {
        this.queryOfficeTreeLeft = queryOfficeTreeLeft;
    }

    public Integer getQueryOfficeTreeRight() {
        return queryOfficeTreeRight;
    }

    public void setQueryOfficeTreeRight(Integer queryOfficeTreeRight) {
        this.queryOfficeTreeRight = queryOfficeTreeRight;
    }

    public String getQueryLoginName() {
        return queryLoginName;
    }

    public void setQueryLoginName(String queryLoginName) {
        this.queryLoginName = queryLoginName;
    }

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public String getQueryEnableFlag() {
        return queryEnableFlag;
    }

    public void setQueryEnableFlag(String queryEnableFlag) {
        this.queryEnableFlag = queryEnableFlag;
    }

    public String getQuerySuperFlag() {
        return querySuperFlag;
    }

    public void setQuerySuperFlag(String querySuperFlag) {
        this.querySuperFlag = querySuperFlag;
    }

    public String getQueryOrderBy() {
        return queryOrderBy;
    }

    public void setQueryOrderBy(String queryOrderBy) {
        this.queryOrderBy = queryOrderBy;
    }
}
