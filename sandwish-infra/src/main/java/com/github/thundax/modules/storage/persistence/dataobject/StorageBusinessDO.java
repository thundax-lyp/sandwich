package com.github.thundax.modules.storage.persistence.dataobject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.thundax.common.persistence.DataEntity;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 存储业务绑定持久化对象。
 */
@NoArgsConstructor
public class StorageBusinessDO extends DataEntity<StorageBusinessDO> {

    private String businessId;
    private String businessType;
    private String businessParams;
    private String publicFlag;

    public StorageBusinessDO(String id) {
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

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getBusinessParams() {
        return businessParams;
    }

    public void setBusinessParams(String businessParams) {
        this.businessParams = businessParams;
    }

    public String getPublicFlag() {
        return publicFlag;
    }

    public void setPublicFlag(String publicFlag) {
        this.publicFlag = publicFlag;
    }

    public static class Query implements Serializable {

        private String businessId;
        private String businessType;
        private String businessParams;
        private String publicFlag;

        public String getBusinessId() {
            return businessId;
        }

        public void setBusinessId(String businessId) {
            this.businessId = businessId;
        }

        public String getBusinessType() {
            return businessType;
        }

        public void setBusinessType(String businessType) {
            this.businessType = businessType;
        }

        public String getBusinessParams() {
            return businessParams;
        }

        public void setBusinessParams(String businessParams) {
            this.businessParams = businessParams;
        }

        public String getPublicFlag() {
            return publicFlag;
        }

        public void setPublicFlag(String publicFlag) {
            this.publicFlag = publicFlag;
        }
    }
}
