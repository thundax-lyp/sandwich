package com.github.thundax.modules.sys.entity.base;

import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.domain.Entity;
import com.github.thundax.common.domain.Signable;
import com.github.thundax.common.domain.Sortable;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.sys.entity.User;
import java.util.Date;
import org.springframework.lang.NonNull;

public abstract class BaseUser extends Entity<User> implements Auditable, Signable, Sortable {

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

    private int priority;
    private String remarks;
    private Date createDate;
    private Date updateDate;
    private String createUserId;
    private String updateUserId;

    public BaseUser() {
        initialize();
    }

    protected void initialize() {}

    public String getId() {
        return EntityIdCodec.toValue(getEntityId());
    }

    public void setId(String id) {
        setEntityId(EntityIdCodec.toDomain(id));
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
        return getId();
    }
}
