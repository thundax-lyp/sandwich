package com.github.thundax.modules.storage.persistence.dataobject;

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
@TableName("assist_storage")
public class StorageDO {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String name;

    private String extendName;

    private String mimeType;

    private String ownerId;

    private String ownerType;

    private String enableFlag;

    private String publicFlag;

    private Integer priority;
    private String remarks;

    private Date createDate;

    private Date updateDate;
}
