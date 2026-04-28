package com.github.thundax.modules.storage.entity.base;

import com.github.thundax.common.persistence.DataEntity;
import com.github.thundax.modules.storage.entity.StorageBusiness;

public abstract class BaseStorageBusiness extends DataEntity<StorageBusiness> {

    private String businessId;
    private String businessType;
    private String businessParams;
    private String publicFlag;

    public BaseStorageBusiness() {
        super();
    }

    public BaseStorageBusiness(String id) {
        super(id);
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
}
