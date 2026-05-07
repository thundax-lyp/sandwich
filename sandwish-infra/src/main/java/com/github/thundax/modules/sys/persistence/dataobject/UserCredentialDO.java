package com.github.thundax.modules.sys.persistence.dataobject;

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
@TableName("sys_user_credential")
public class UserCredentialDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long userId;

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
