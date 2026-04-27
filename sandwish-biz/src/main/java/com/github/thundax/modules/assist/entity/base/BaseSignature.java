package com.github.thundax.modules.assist.entity.base;

import com.github.thundax.common.persistence.DataEntity;
import com.github.thundax.modules.assist.entity.Signature;

/**
 * 签名存储基类
 *
 * @author wdit
 */
public abstract class BaseSignature extends DataEntity<Signature> {

    /** 业务分类 * */
    private String businessType;
    /** 业务主键 * */
    private String businessId;
    /** 签名数据 * */
    private String signature;
    /** 验签结果（0：未验签，1：验签成功，2：验签失败） * */
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
