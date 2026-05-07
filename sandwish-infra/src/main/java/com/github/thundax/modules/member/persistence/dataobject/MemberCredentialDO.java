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
@TableName("member_credential")
public class MemberCredentialDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long memberId;

    private Long identityId;

    private String credentialType;

    private String credentialValue;

    private String status;

    private Boolean needChangePassword;

    private Integer failedCount;

    private Integer failedLimit;

    private Date lockedUntil;

    private Date expiresAt;

    private Date lastVerifiedAt;
}
