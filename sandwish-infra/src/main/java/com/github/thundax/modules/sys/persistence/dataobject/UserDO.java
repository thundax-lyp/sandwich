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

    @TableField(exist = false)
    public static final String DEL_FLAG_NORMAL = "0";

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("office_id")
    private String officeId;

    @TableField("login_name")
    private String loginName;

    @TableField("login_pass")
    private String loginPass;

    @TableField(typeHandler = DefaultEncryptTypeHandler.class)
    private String email;

    @TableField(typeHandler = DefaultEncryptTypeHandler.class)
    private String mobile;

    private String tel;
    private String name;
    private Integer ranks;

    @TableField("register_date")
    private Date registerDate;

    @TableField("register_ip")
    private String registerIp;

    @TableField("last_login_date")
    private Date lastLoginDate;

    @TableField("last_login_ip")
    private String lastLoginIp;

    @TableField("login_count")
    private Integer loginCount;

    @TableField("super_flag")
    private String superFlag;

    @TableField("admin_flag")
    private String adminFlag;

    @TableField("enable_flag")
    private String enableFlag;

    @TableField("sso_login_name")
    private String ssoLoginName;

    private Integer priority;
    private String remarks;

    @TableField("create_date")
    private Date createDate;

    @TableField("create_by")
    private String createBy;

    @TableField("update_date")
    private Date updateDate;

    @TableField("update_by")
    private String updateBy;

    @TableField("del_flag")
    private String delFlag;
}
