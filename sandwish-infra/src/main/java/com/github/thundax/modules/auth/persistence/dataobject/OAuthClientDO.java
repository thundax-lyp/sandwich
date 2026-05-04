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
@TableName("auth_oauth_client")
public class OAuthClientDO {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String clientId;

    private String clientSecretHash;

    private String clientName;

    private String clientType;

    private String grantTypes;

    private String scopes;

    private String redirectUris;

    private long accessTokenTtlSeconds;

    private long refreshTokenTtlSeconds;

    private String status;

    private String contact;

    private String remark;

    private Date createDate;

    private String createBy;

    private Date updateDate;

    private String updateBy;
}
