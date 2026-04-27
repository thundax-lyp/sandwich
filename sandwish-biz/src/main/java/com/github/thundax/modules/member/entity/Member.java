package com.github.thundax.modules.member.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.thundax.common.config.Global;
import com.github.thundax.common.utils.DateUtils;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.modules.member.entity.base.BaseMember;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * @author wdit
 */
public class Member extends BaseMember {

    public static final String BEAN_NAME = "Member";

    public static final String MALE = "1";
    public static final String FEMALE = "0";

    public Member() {
        super();
    }

    public Member(String id) {
        super(id);
    }

    @Override
    public void setEnableFlag(String enableFlag) {
        super.setEnableFlag(StringUtils.equals(Global.ENABLE, enableFlag) ? Global.ENABLE : Global.DISABLE);
    }

    public boolean isEnable() {
        return StringUtils.equals(Global.ENABLE, getEnableFlag());
    }

    @JsonIgnore
    public boolean isMale() {
        return MALE.equals(getGender());
    }

    private Query query;

    @JsonIgnore
    public Query getQuery() {
        return this.query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public static class Query implements Serializable {

        public static final String PROP_ENABLE_FLAG = "enableFlag";
        public static final String PROP_EMAIL = "email";
        public static final String PROP_NAME = "name";
        public static final String PROP_REMARKS = "remarks";

        public static final String PROP_BEGIN_REGISTER_DATE = "beginRegisterDate";
        public static final String PROP_END_REGISTER_DATE = "endRegisterDate";
        public static final String PROP_BEGIN_LOGIN_DATE = "beginLoginDate";
        public static final String PROP_END_LOGIN_DATE = "endLoginDate";
        public static final String PROP_MOBILE = "mobile";

        private String enableFlag;
        private String email;
        private String name;
        private String remarks; // 按照rank查询

        private Date beginRegisterDate;
        private Date endRegisterDate;
        private Date beginLoginDate;
        private Date endLoginDate;
        /**
         * 一网通办id
         */
        private String ywtbId;

        /**
         * 证件号码
         */
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

        @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
        public Date getBeginRegisterDate() {
            return beginRegisterDate;
        }

        public void setBeginRegisterDate(Date beginRegisterDate) {
            if (beginRegisterDate != null) {
                beginRegisterDate = DateUtils.truncate(beginRegisterDate, Calendar.DATE);
            }
            this.beginRegisterDate = beginRegisterDate;
        }

        @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
        public Date getEndRegisterDate() {
            return endRegisterDate;
        }

        public void setEndRegisterDate(Date endRegisterDate) {
            if (endRegisterDate != null) {
                endRegisterDate = DateUtils.addSeconds(DateUtils.ceiling(endRegisterDate, Calendar.DATE), -1);
            }
            this.endRegisterDate = endRegisterDate;
        }

        @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
        public Date getBeginLoginDate() {
            return beginLoginDate;
        }

        public void setBeginLoginDate(Date beginLoginDate) {
            if (beginLoginDate != null) {
                beginLoginDate = DateUtils.truncate(beginLoginDate, Calendar.DATE);
            }
            this.beginLoginDate = beginLoginDate;
        }

        @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
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
