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


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_role")
public class RoleDO {

    public static final String DEL_FLAG_NORMAL = "0";

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String name;

    @TableField("admin_flag")
    private String adminFlag;

    @TableField("enable_flag")
    private String enableFlag;

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
