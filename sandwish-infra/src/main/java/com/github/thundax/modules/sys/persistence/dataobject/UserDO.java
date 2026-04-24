package com.github.thundax.modules.sys.persistence.dataobject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.common.persistence.AdminDataEntity;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 用户持久化对象。
 */
@NoArgsConstructor
public class UserDO extends AdminDataEntity<UserDO> {

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

    public UserDO(String id) {
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

    public static class Query implements Serializable {

        private String officeId;
        private Integer officeTreeLeft;
        private Integer officeTreeRight;
        private String loginName;
        private String name;
        private String enableFlag;
        private String superFlag;
        private String orderBy;

        public String getOfficeId() {
            return officeId;
        }

        public void setOfficeId(String officeId) {
            this.officeId = officeId;
        }

        public Integer getOfficeTreeLeft() {
            return officeTreeLeft;
        }

        public void setOfficeTreeLeft(Integer officeTreeLeft) {
            this.officeTreeLeft = officeTreeLeft;
        }

        public Integer getOfficeTreeRight() {
            return officeTreeRight;
        }

        public void setOfficeTreeRight(Integer officeTreeRight) {
            this.officeTreeRight = officeTreeRight;
        }

        public String getLoginName() {
            return loginName;
        }

        public void setLoginName(String loginName) {
            this.loginName = loginName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEnableFlag() {
            return enableFlag;
        }

        public void setEnableFlag(String enableFlag) {
            this.enableFlag = enableFlag;
        }

        public String getSuperFlag() {
            return superFlag;
        }

        public void setSuperFlag(String superFlag) {
            this.superFlag = superFlag;
        }

        public String getOrderBy() {
            return orderBy;
        }

        public void setOrderBy(String orderBy) {
            this.orderBy = orderBy;
        }
    }
}
