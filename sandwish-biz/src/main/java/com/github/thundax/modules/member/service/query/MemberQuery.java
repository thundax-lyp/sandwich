package com.github.thundax.modules.member.service.query;

import com.github.thundax.modules.member.entity.enums.MemberStatus;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

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
}
