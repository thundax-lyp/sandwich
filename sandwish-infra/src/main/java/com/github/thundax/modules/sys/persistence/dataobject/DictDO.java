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

/** 字典持久化对象。 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_dict")
public class DictDO {

    @TableField(exist = false)
    public static final String DEL_FLAG_NORMAL = "0";

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField(exist = false)
    private boolean isNewRecord;

    private String type;
    private String label;
    private String value;
    private Integer priority;
    private String remarks;
    private Date createDate;

    @TableField("create_by")
    private String createUserId;

    private Date updateDate;

    @TableField("update_by")
    private String updateUserId;

    private String delFlag;
}
