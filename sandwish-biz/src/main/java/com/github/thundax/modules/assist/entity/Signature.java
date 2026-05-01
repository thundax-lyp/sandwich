package com.github.thundax.modules.assist.entity;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
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
    private EntityId id;

    private String businessType;

    private String businessId;

    private String signature;

    private String isVerifySign;
    private int priority;
    private String remarks;
    private Date createDate;
    private Date updateDate;

    public static final String BEAN_NAME = "Signature";

    public Signature(String id, String businessType, String businessId) {
        setId(EntityIdCodec.toDomain(id));
        this.setBusinessType(businessType);
        this.setBusinessId(businessId);
    }

    private Query query;

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
