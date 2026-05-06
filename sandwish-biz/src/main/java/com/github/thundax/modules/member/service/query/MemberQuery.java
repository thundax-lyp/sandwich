package com.github.thundax.modules.member.service.query;

import com.github.thundax.modules.member.entity.enums.MemberStatus;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberQuery {
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
