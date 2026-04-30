package com.github.thundax.modules.assist.entity.base;

import com.github.thundax.common.domain.Entity;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.assist.entity.Signature;
import java.util.Date;

/**
 * 签名存储基类
 */
public abstract class BaseSignature extends Entity<Signature> {

    private String businessType;

    private String businessId;

    private String signature;

    private String isVerifySign;
    private Integer priority;
    private String remarks;
    private Date createDate;
    private Date updateDate;

    public BaseSignature() {}

    public BaseSignature(String id) {
        setEntityId(EntityIdCodec.toDomain(id));
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getIsVerifySign() {
        return isVerifySign;
    }

    public void setIsVerifySign(String isVerifySign) {
        this.isVerifySign = isVerifySign;
    }

    public Integer getPriority() {
        return priority == null ? 0 : priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority != null && priority >= 0 ? priority : 0;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
}
