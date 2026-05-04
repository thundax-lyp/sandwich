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

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String userId;

    private String identityId;

    private String credentialType;

    private String credentialValue;

    private String status;

    private Boolean needChangePassword;

    private Integer failedCount;

    private Integer failedLimit;

    private Date lockedUntil;

    private Date expiresAt;

    private Date lastVerifiedAt;

    private Date createDate;

    private String createBy;

    private Date updateDate;

    private String updateBy;
}
