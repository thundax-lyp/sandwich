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

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String sessionId;

    private String token;

    private String userId;

    private String identityId;

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
