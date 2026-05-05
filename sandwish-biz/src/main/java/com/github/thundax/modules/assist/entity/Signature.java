package com.github.thundax.modules.assist.entity;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 签名存储
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Signature {
    public static final String BEAN_NAME = "Signature";

    private EntityId id;

    private String businessType;

    private String businessId;

    private String signature;

    private String isVerifySign;
    private int priority;
    private String remarks;
    private Date createDate;
    private Date updateDate;

    public Signature(String id, String businessType, String businessId) {
        setId(EntityIdCodec.toDomain(id));
        this.setBusinessType(businessType);
        this.setBusinessId(businessId);
    }
}
