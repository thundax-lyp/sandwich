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
@TableName("sys_menu")
public class MenuDO {

    public static final String ROOT_ID = "ROOT";

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String parentId;

    private Integer lft;

    private Integer rgt;

    private String name;

    private String perms;

    private Integer ranks;

    private String displayFlag;

    private String displayParams;

    private String url;

    private String target;

    private Integer priority;

    private String remarks;

    private Date createDate;

    private String createBy;

    private Date updateDate;

    private String updateBy;


    public Integer treeSpan() {
        return this.rgt - this.lft + 1;
    }
}
