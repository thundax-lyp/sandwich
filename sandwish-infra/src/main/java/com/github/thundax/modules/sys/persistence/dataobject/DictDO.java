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
@TableName("sys_dict")
public class DictDO {

    @TableField(exist = false)
    public static final String DEL_FLAG_NORMAL = "0";

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String type;
    private String label;
    private String value;
    private Integer priority;
    private String remarks;
    private Date createDate;

    @TableField("create_by")
    private String createBy;

    private Date updateDate;

    @TableField("update_by")
    private String updateBy;

    private String delFlag;
}
