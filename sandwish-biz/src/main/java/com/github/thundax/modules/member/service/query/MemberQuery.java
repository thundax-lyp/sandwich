package com.github.thundax.modules.member.service.query;

import com.github.thundax.modules.member.entity.enums.MemberStatus;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

@Getter
@Setter
public class MemberQuery implements Serializable {
    private MemberStatus status;
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

    public void setStatus(String status) {
        this.status = StringUtils.isBlank(status) ? null : MemberStatus.from(status);
    }

    public void setStatus(MemberStatus status) {
        this.status = status;
    }

    public void setBeginRegisterDate(Date beginRegisterDate) {
        if (beginRegisterDate != null) {
            beginRegisterDate = DateUtils.truncate(beginRegisterDate, Calendar.DATE);
        }
        this.beginRegisterDate = beginRegisterDate;
    }

    public void setEndRegisterDate(Date endRegisterDate) {
        if (endRegisterDate != null) {
            endRegisterDate = DateUtils.addSeconds(DateUtils.ceiling(endRegisterDate, Calendar.DATE), -1);
        }
        this.endRegisterDate = endRegisterDate;
    }

    public void setBeginLoginDate(Date beginLoginDate) {
        if (beginLoginDate != null) {
            beginLoginDate = DateUtils.truncate(beginLoginDate, Calendar.DATE);
        }
        this.beginLoginDate = beginLoginDate;
    }

    public void setEndLoginDate(Date endLoginDate) {
        if (endLoginDate != null) {
            endLoginDate = DateUtils.addSeconds(DateUtils.ceiling(endLoginDate, Calendar.DATE), -1);
        }
        this.endLoginDate = endLoginDate;
    }
}
