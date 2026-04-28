package com.github.thundax.modules.sys.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.thundax.common.persistence.entity.DefaultEncryptTypeHandler;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sys_user", autoResultMap = true)
public class UserDO {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String officeId;

    private String loginName;

    private String loginPass;

    @TableField(typeHandler = DefaultEncryptTypeHandler.class)
    private String email;

    @TableField(typeHandler = DefaultEncryptTypeHandler.class)
    private String mobile;

    private String tel;
    private String name;
    private Integer ranks;

    private Date registerDate;

    private String registerIp;

    private Date lastLoginDate;

    private String lastLoginIp;

    private Integer loginCount;

    private String superFlag;

    private String adminFlag;

    private String enableFlag;

    private String ssoLoginName;

    private Integer priority;
    private String remarks;

    private Date createDate;

    private String createBy;

    private Date updateDate;

    private String updateBy;
}
