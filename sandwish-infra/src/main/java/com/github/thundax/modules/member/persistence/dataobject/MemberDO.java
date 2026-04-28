package com.github.thundax.modules.member.persistence.dataobject;

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

/** 会员持久化对象。 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tb_member", autoResultMap = true)
public class MemberDO {

    @TableField(exist = false)
    public static final String DEL_FLAG_NORMAL = "0";

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField("login_name")
    private String loginName;

    @TableField("login_pass")
    private String loginPass;

    @TableField(typeHandler = DefaultEncryptTypeHandler.class)
    private String email;

    private String name;
    private String gender;

    @TableField(typeHandler = DefaultEncryptTypeHandler.class)
    private String mobile;

    @TableField(typeHandler = DefaultEncryptTypeHandler.class)
    private String address;

    private String zipcode;

    @TableField("enable_flag")
    private String enableFlag;

    @TableField("register_ip")
    private String registerIp;

    @TableField("register_date")
    private Date registerDate;

    @TableField("last_login_ip")
    private String lastLoginIp;

    @TableField("last_login_date")
    private Date lastLoginDate;

    @TableField("ywtb_id")
    private String ywtbId;

    @TableField("login_count")
    private int loginCount;

    private Integer priority;
    private String remarks;

    @TableField("create_date")
    private Date createDate;

    @TableField("create_by")
    private String createUserId;

    @TableField("update_date")
    private Date updateDate;

    @TableField("update_by")
    private String updateUserId;

    @TableField("del_flag")
    private String delFlag;
}
