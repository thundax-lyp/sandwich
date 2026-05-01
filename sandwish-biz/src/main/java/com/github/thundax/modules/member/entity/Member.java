package com.github.thundax.modules.member.entity;

import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.domain.Sortable;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.member.entity.enums.MemberStatus;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member implements Auditable, Sortable {
    private EntityId id;

    private String loginName;
    private String loginPass;

    private String email;
    private String name;
    private String gender;
    private String mobile;
    private String address;
    private String zipcode;

    private MemberStatus status = MemberStatus.ENABLED;

    private String registerIp;
    private Date registerDate;
    private String lastLoginIp;
    private Date lastLoginDate;

    private String ywtbId;

    private int loginCount;
    private int priority;
    private String remarks;
    private Date createDate;
    private Date updateDate;
    private String createUserId;
    private String updateUserId;

    public static final String BEAN_NAME = "Member";

    public static final String MALE = "1";
    public static final String FEMALE = "0";

    public void setStatus(String status) {
        this.status = StringUtils.isBlank(status) ? null : MemberStatus.from(status);
    }

    public void setStatus(MemberStatus status) {
        this.status = status;
    }

    public boolean isEnable() {
        return MemberStatus.ENABLED == getStatus();
    }

    public boolean isMale() {
        return MALE.equals(getGender());
    }

    private Query query;

    public Query getQuery() {
        return this.query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public static class Query implements Serializable {

        public static final String PROP_STATUS = "status";
        public static final String PROP_EMAIL = "email";
        public static final String PROP_NAME = "name";
        public static final String PROP_REMARKS = "remarks";

        public static final String PROP_BEGIN_REGISTER_DATE = "beginRegisterDate";
        public static final String PROP_END_REGISTER_DATE = "endRegisterDate";
        public static final String PROP_BEGIN_LOGIN_DATE = "beginLoginDate";
        public static final String PROP_END_LOGIN_DATE = "endLoginDate";
        public static final String PROP_MOBILE = "mobile";

        private MemberStatus status;
        private String email;
        private String name;
        private String remarks; // 按照rank查询

        private Date beginRegisterDate;
        private Date endRegisterDate;
        private Date beginLoginDate;
        private Date endLoginDate;

        private String ywtbId;

        private String zjhm;

        private String mobile;

        public MemberStatus getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = StringUtils.isBlank(status) ? null : MemberStatus.from(status);
        }

        public void setStatus(MemberStatus status) {
            this.status = status;
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
            if (beginRegisterDate != null) {
                beginRegisterDate = DateUtils.truncate(beginRegisterDate, Calendar.DATE);
            }
            this.beginRegisterDate = beginRegisterDate;
        }

        public Date getEndRegisterDate() {
            return endRegisterDate;
        }

        public void setEndRegisterDate(Date endRegisterDate) {
            if (endRegisterDate != null) {
                endRegisterDate = DateUtils.addSeconds(DateUtils.ceiling(endRegisterDate, Calendar.DATE), -1);
            }
            this.endRegisterDate = endRegisterDate;
        }

        public Date getBeginLoginDate() {
            return beginLoginDate;
        }

        public void setBeginLoginDate(Date beginLoginDate) {
            if (beginLoginDate != null) {
                beginLoginDate = DateUtils.truncate(beginLoginDate, Calendar.DATE);
            }
            this.beginLoginDate = beginLoginDate;
        }

        public Date getEndLoginDate() {
            return endLoginDate;
        }

        public void setEndLoginDate(Date endLoginDate) {
            if (endLoginDate != null) {
                endLoginDate = DateUtils.addSeconds(DateUtils.ceiling(endLoginDate, Calendar.DATE), -1);
            }
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
