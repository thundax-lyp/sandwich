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
@TableName("auth_user_identity")
public class UserIdentityDO {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String userId;

    private String identityType;

    private String identityValue;

    private String status;

    private Date createDate;

    private String createBy;

    private Date updateDate;

    private String updateBy;
}
