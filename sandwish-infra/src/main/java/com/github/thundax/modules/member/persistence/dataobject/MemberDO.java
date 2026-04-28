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


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tb_member", autoResultMap = true)
public class MemberDO {


    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String loginName;

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

    private String enableFlag;

    private String registerIp;

    private Date registerDate;

    private String lastLoginIp;

    private Date lastLoginDate;

    private String ywtbId;

    private int loginCount;

    private Integer priority;
    private String remarks;

    private Date createDate;

    private String createBy;

    private Date updateDate;

    private String updateBy;

}
