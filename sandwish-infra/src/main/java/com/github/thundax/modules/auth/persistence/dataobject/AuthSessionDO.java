package com.github.thundax.modules.auth.persistence.dataobject;

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
@TableName("auth_session")
public class AuthSessionDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private String token;

    private String principalType;

    private Long principalId;

    private Long identityId;

    private String identityType;

    private String loginType;

    private String status;

    private Date issuedAt;

    private Date lastAccessTime;

    private Date expireAt;

    private Date logoutAt;

    private String invalidateReason;
}
