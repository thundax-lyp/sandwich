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

/** 机构持久化对象。 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_office")
public class OfficeDO {

    public static final String DEL_FLAG_NORMAL = "0";

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField("parent_id")
    private String parentId;

    private Integer lft;

    private Integer rgt;

    private String name;

    @TableField("short_name")
    private String shortName;

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
