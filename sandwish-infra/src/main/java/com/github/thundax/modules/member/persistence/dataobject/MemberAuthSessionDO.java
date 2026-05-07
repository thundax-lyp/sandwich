package com.github.thundax.modules.member.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("member_auth_session")
public class MemberAuthSessionDO {
    @TableId(type = IdType.INPUT)
    private Long id;

    private String sessionId;
    private Long memberId;
    private Long identityId;
    private String identityType;
    private String loginType;
    private String status;
    private Date issuedAt;
    private Date lastAccessTime;
    private Date expireAt;
    private Date logoutAt;
    private String invalidateReason;
    private Date createDate;
    private String createBy;
    private Date updateDate;
    private String updateBy;
}
