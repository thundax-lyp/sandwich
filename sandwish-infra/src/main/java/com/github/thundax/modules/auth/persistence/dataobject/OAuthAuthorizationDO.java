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
@TableName("auth_oauth_authorization")
public class OAuthAuthorizationDO {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String authorizationCode;

    private String clientId;

    private String tenantId;

    private String userId;

    private String redirectUri;

    private String scopes;

    private String state;

    private String codeChallenge;

    private String codeChallengeMethod;

    private Date issuedAt;

    private Date expireAt;

    private boolean used;

    private Date createDate;

    private String createBy;

    private Date updateDate;

    private String updateBy;
}
