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

/** 菜单持久化对象。 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_menu")
public class MenuDO {

    public static final String DEL_FLAG_NORMAL = "0";
    public static final String ROOT_ID = "ROOT";

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("parent_id")
    private String parentId;

    private Integer lft;

    private Integer rgt;

    private String name;

    private String perms;

    private Integer ranks;

    @TableField("display_flag")
    private String displayFlag;

    @TableField("display_params")
    private String displayParams;

    private String url;

    private String target;

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

    public Integer treeSpan() {
        return this.rgt - this.lft + 1;
    }
}
