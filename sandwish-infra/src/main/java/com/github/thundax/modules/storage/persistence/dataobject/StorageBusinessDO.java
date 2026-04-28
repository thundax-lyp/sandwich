package com.github.thundax.modules.storage.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("assist_storage_business")
public class StorageBusinessDO {

    @TableId(value = "file_id", type = IdType.INPUT)
    private String storageId;

    private String businessId;

    private String businessType;

    private String businessParams;

    private String publicFlag;





}
