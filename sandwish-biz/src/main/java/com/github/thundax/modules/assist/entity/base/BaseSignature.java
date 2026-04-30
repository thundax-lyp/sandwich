package com.github.thundax.modules.assist.entity.base;

import com.github.thundax.common.id.EntityId;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 签名存储基类
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseSignature {
    private EntityId id;

    private String businessType;

    private String businessId;

    private String signature;

    private String isVerifySign;
    private int priority;
    private String remarks;
    private Date createDate;
    private Date updateDate;

    public void setPriority(int priority) {
        this.priority = priority >= 0 ? priority : 0;
    }
}
