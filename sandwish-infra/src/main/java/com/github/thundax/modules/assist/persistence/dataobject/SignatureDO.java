package com.github.thundax.modules.assist.persistence.dataobject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.thundax.common.persistence.DataEntity;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 签名持久化对象。
 */
@NoArgsConstructor
public class SignatureDO extends DataEntity<SignatureDO> {

    private String businessType;
    private String businessId;
    private String signature;
    private String isVerifySign;

    public SignatureDO(String id) {
        super(id);
    }

    @Override
    protected Object createQueryObject() {
        return new Query();
    }

    @JsonIgnore
    public Query getQuery() {
        return (Query) this.query;
    }

    public void setQuery(Query query) {
        this.query = query;
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

    public static class Query implements Serializable {

        private String businessType;
        private String businessId;
        private String isVerifySign;
        private List<String> businessIdList;

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

        public String getIsVerifySign() {
            return isVerifySign;
        }

        public void setIsVerifySign(String isVerifySign) {
            this.isVerifySign = isVerifySign;
        }

        public List<String> getBusinessIdList() {
            return businessIdList;
        }

        public void setBusinessIdList(List<String> businessIdList) {
            this.businessIdList = businessIdList;
        }
    }
}
