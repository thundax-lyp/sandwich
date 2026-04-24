package com.github.thundax.modules.member.persistence.dataobject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.thundax.common.persistence.AdminDataEntity;

import java.io.Serializable;
import java.util.Date;

/**
 * 会员持久化对象。
 */
public class MemberDO extends AdminDataEntity<MemberDO> {

    private static final long serialVersionUID = 1L;

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

    public MemberDO() {
        super();
    }

    public MemberDO(String id) {
        super(id);
    }

    @Override
    protected Object createQueryObject() {
        return new Query();
    }

    @JsonIgnore
    public Query getQuery() {
        return (Query) this.query;
    }

    public void setQuery(Query query) {
        this.query = query;
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

    public static class Query implements Serializable {

        private static final long serialVersionUID = 1L;

        private String enableFlag;
        private String email;
        private String name;
        private String remarks;
        private Date beginRegisterDate;
        private Date endRegisterDate;
        private Date beginLoginDate;
        private Date endLoginDate;
        private String ywtbId;
        private String zjhm;
        private String mobile;

        public String getEnableFlag() {
            return enableFlag;
        }

        public void setEnableFlag(String enableFlag) {
            this.enableFlag = enableFlag;
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

        public String getRemarks() {
            return remarks;
        }

        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }

        public Date getBeginRegisterDate() {
            return beginRegisterDate;
        }

        public void setBeginRegisterDate(Date beginRegisterDate) {
            this.beginRegisterDate = beginRegisterDate;
        }

        public Date getEndRegisterDate() {
            return endRegisterDate;
        }

        public void setEndRegisterDate(Date endRegisterDate) {
            this.endRegisterDate = endRegisterDate;
        }

        public Date getBeginLoginDate() {
            return beginLoginDate;
        }

        public void setBeginLoginDate(Date beginLoginDate) {
            this.beginLoginDate = beginLoginDate;
        }

        public Date getEndLoginDate() {
            return endLoginDate;
        }

        public void setEndLoginDate(Date endLoginDate) {
            this.endLoginDate = endLoginDate;
        }

        public String getYwtbId() {
            return ywtbId;
        }

        public void setYwtbId(String ywtbId) {
            this.ywtbId = ywtbId;
        }

        public String getZjhm() {
            return zjhm;
        }

        public void setZjhm(String zjhm) {
            this.zjhm = zjhm;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }
    }
}
