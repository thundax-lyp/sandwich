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

/** 存储文件持久化对象。 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("assist_storage")
public class StorageDO {

    @TableField(exist = false)
    public static final String DEL_FLAG_NORMAL = "0";

    @TableId(type = IdType.INPUT)
    private String id;

    private String name;

    @TableField("extend_name")
    private String extendName;

    @TableField("mime_type")
    private String mimeType;

    @TableField(exist = false)
    private String businessId;

    @TableField(exist = false)
    private String businessType;

    @TableField(exist = false)
    private String businessParams;

    @TableField("owner_id")
    private String ownerId;

    @TableField("owner_type")
    private String ownerType;

    @TableField("enable_flag")
    private String enableFlag;

    @TableField("public_flag")
    private String publicFlag;

    private Integer priority;
    private String remarks;

    @TableField("create_date")
    private Date createDate;

    @TableField("update_date")
    private Date updateDate;

    @TableField("del_flag")
    private String delFlag;
}
