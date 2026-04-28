package com.github.thundax.modules.sys.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 用户加密信息持久化对象。 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_user_encrypt")
public class UserEncryptDO {

    @TableId(value = "id", type = IdType.INPUT)
    private String userId;

    @TableField("login_pass")
    private String loginPass;

    private String email;

    private String mobile;

    private String tel;

    @TableField(exist = false)
    private Integer priority;

    @TableField(exist = false)
    private String remarks;

    @TableField("create_date")
    private Date createDate;

    @TableField("create_by")
    private String createBy;

    @TableField("update_date")
    private Date updateDate;

    @TableField("update_by")
    private String updateBy;

    @TableField(exist = false)
    private String delFlag;
}
