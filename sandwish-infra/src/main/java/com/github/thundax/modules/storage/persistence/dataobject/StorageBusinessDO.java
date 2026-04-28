package com.github.thundax.modules.storage.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 存储业务绑定持久化对象。 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("assist_storage_business")
public class StorageBusinessDO {

    @TableId(value = "file_id", type = IdType.INPUT)
    private String id;

    @TableField(exist = false)
    private boolean isNewRecord;

    @TableField("business_id")
    private String businessId;

    @TableField("business_type")
    private String businessType;

    @TableField("business_params")
    private String businessParams;

    @TableField("public_flag")
    private String publicFlag;

    @TableField(exist = false)
    private Integer priority;

    @TableField(exist = false)
    private String remarks;

    @TableField(exist = false)
    private Date createDate;

    @TableField(exist = false)
    private Date updateDate;

    @TableField(exist = false)
    private String delFlag;
}
