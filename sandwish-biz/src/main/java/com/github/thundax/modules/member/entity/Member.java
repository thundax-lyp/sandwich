package com.github.thundax.modules.member.entity;

import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.domain.Sortable;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.member.entity.enums.MemberStatus;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member implements Auditable, Sortable {
    public static final String BEAN_NAME = "Member";

    public static final String MALE = "1";
    public static final String FEMALE = "0";

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

    private int loginCount;
    private int priority;
    private String remarks;
    private Date createDate;
    private Date updateDate;
    private String createUserId;
    private String updateUserId;

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
}
