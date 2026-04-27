package com.github.thundax.modules.member.entity.base;

import com.github.thundax.common.persistence.AdminDataEntity;
import com.github.thundax.modules.member.entity.Member;
import java.util.Date;

/** @author wdit */
public class BaseMember extends AdminDataEntity<Member> {

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
    /** *一网通办id */
    private String ywtbId;
    /** 登录次数* */
    private int loginCount;

    public BaseMember() {
        super();
    }

    public BaseMember(String id) {
        super(id);
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
}
