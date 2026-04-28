package com.github.thundax.modules.sys.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.thundax.common.persistence.entity.DefaultEncryptTypeHandler;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 用户持久化对象。 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sys_user", autoResultMap = true)
public class UserDO {

    @TableField(exist = false)
    public static final String DEL_FLAG_NORMAL = "0";

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField(exist = false)
    private boolean isNewRecord;

    @TableField("office_id")
    private String officeId;

    @TableField("login_name")
    private String loginName;

    @TableField("login_pass")
    private String loginPass;

    @TableField(typeHandler = DefaultEncryptTypeHandler.class)
    private String email;

    @TableField(typeHandler = DefaultEncryptTypeHandler.class)
    private String mobile;

    private String tel;
    private String name;
    private Integer ranks;

    @TableField("register_date")
    private Date registerDate;

    @TableField("register_ip")
    private String registerIp;

    @TableField("last_login_date")
    private Date lastLoginDate;

    @TableField("last_login_ip")
    private String lastLoginIp;

    @TableField("login_count")
    private Integer loginCount;

    @TableField("super_flag")
    private String superFlag;

    @TableField("admin_flag")
    private String adminFlag;

    @TableField("enable_flag")
    private String enableFlag;

    @TableField("sso_login_name")
    private String ssoLoginName;

    private Integer priority;
    private String remarks;

    @TableField("create_date")
    private Date createDate;

    @TableField("create_by")
    private String createUserId;

    @TableField("update_date")
    private Date updateDate;

    @TableField("update_by")
    private String updateUserId;

    @TableField("del_flag")
    private String delFlag;

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

}
