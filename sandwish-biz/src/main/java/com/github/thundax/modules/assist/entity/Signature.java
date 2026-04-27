package com.github.thundax.modules.assist.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.thundax.modules.assist.entity.base.BaseSignature;
import java.io.Serializable;
import java.util.List;

/**
 * 签名存储
 *
 * @author wdit
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Signature extends BaseSignature {

    public static final String BEAN_NAME = "Signature";

    public Signature() {
        super();
    }

    public Signature(String id, String businessType, String businessId) {
        super(id);
        this.setBusinessType(businessType);
        this.setBusinessId(businessId);
    }

    private Query query;

    @JsonIgnore
    public Query getQuery() {
        return this.query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public static class Query implements Serializable {

        public static final String PROP_BUSINESS_TYPE = "businessType";
        public static final String PROP_BUSINESS_ID = "businessId";
        public static final String PROP_BUSINESS_ID_LIST = "businessIdList";
        public static final String PROP_BUSINESS_VERIFY = "isVerifySign";

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
