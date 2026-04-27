package com.github.thundax.modules.member.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * 会员持久化对象。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_member")
public class MemberDO {

    public static final String DEL_FLAG_NORMAL = "0";

    private String id;
    private boolean isNewRecord;

    private String loginName;
    private String loginPass;
    private String email;
    private String name;
    private String gender;
    private String mobile;
    private String address;
    private String zipcode;
    private String enableFlag;
    private String registerIp;
    private Date registerDate;
    private String lastLoginIp;
    private Date lastLoginDate;
    private String ywtbId;
    private int loginCount;
    private Integer priority;
    private String remarks;
    private Date createDate;
    private String createUserId;
    private Date updateDate;
    private String updateUserId;
    private String delFlag;
    private String queryEnableFlag;
    private String queryEmail;
    private String queryName;
    private String queryRemarks;
    private Date queryBeginRegisterDate;
    private Date queryEndRegisterDate;
    private Date queryBeginLoginDate;
    private Date queryEndLoginDate;
    private String queryYwtbId;
    private String queryZjhm;
    private String queryMobile;

    public MemberDO(String id) {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getEnableFlag() {
        return enableFlag;
    }

    public void setEnableFlag(String enableFlag) {
        this.enableFlag = enableFlag;
    }

    public String getRegisterIp() {
        return registerIp;
    }

    public void setRegisterIp(String registerIp) {
        this.registerIp = registerIp;
    }

    public Date getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(Date registerDate) {
        this.registerDate = registerDate;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public Date getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public String getYwtbId() {
        return ywtbId;
    }

    public void setYwtbId(String ywtbId) {
        this.ywtbId = ywtbId;
    }

    public int getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(int loginCount) {
        this.loginCount = loginCount;
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

    public String getQueryEmail() {
        return queryEmail;
    }

    public void setQueryEmail(String queryEmail) {
        this.queryEmail = queryEmail;
    }

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public String getQueryRemarks() {
        return queryRemarks;
    }

    public void setQueryRemarks(String queryRemarks) {
        this.queryRemarks = queryRemarks;
    }

    public Date getQueryBeginRegisterDate() {
        return queryBeginRegisterDate;
    }

    public void setQueryBeginRegisterDate(Date queryBeginRegisterDate) {
        this.queryBeginRegisterDate = queryBeginRegisterDate;
    }

    public Date getQueryEndRegisterDate() {
        return queryEndRegisterDate;
    }

    public void setQueryEndRegisterDate(Date queryEndRegisterDate) {
        this.queryEndRegisterDate = queryEndRegisterDate;
    }

    public Date getQueryBeginLoginDate() {
        return queryBeginLoginDate;
    }

    public void setQueryBeginLoginDate(Date queryBeginLoginDate) {
        this.queryBeginLoginDate = queryBeginLoginDate;
    }

    public Date getQueryEndLoginDate() {
        return queryEndLoginDate;
    }

    public void setQueryEndLoginDate(Date queryEndLoginDate) {
        this.queryEndLoginDate = queryEndLoginDate;
    }

    public String getQueryYwtbId() {
        return queryYwtbId;
    }

    public void setQueryYwtbId(String queryYwtbId) {
        this.queryYwtbId = queryYwtbId;
    }

    public String getQueryZjhm() {
        return queryZjhm;
    }

    public void setQueryZjhm(String queryZjhm) {
        this.queryZjhm = queryZjhm;
    }

    public String getQueryMobile() {
        return queryMobile;
    }

    public void setQueryMobile(String queryMobile) {
        this.queryMobile = queryMobile;
    }
}
