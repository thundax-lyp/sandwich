package com.github.thundax.modules.storage.persistence.dataobject;

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
public class StoredObjectReferenceDO {

    private String fileId;

    private String referenceOwnerId;

    private String referenceOwnerType;

    private String businessParams;

    private String referenceStatus;
}
