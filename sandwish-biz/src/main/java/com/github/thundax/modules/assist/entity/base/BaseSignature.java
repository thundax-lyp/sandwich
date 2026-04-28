package com.github.thundax.modules.assist.entity.base;

import com.github.thundax.common.persistence.DataEntity;
import com.github.thundax.modules.assist.entity.Signature;

/**
 * 签名存储基类
 *
 * @author wdit
 */
public abstract class BaseSignature extends DataEntity<Signature> {


    private String businessType;

    private String businessId;

    private String signature;

    private String isVerifySign;

    public BaseSignature() {
        super();
    }

    public BaseSignature(String id) {
        super(id);
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
}
