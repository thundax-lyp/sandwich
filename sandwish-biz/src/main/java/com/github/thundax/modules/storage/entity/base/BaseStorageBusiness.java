package com.github.thundax.modules.storage.entity.base;

import com.github.thundax.common.id.EntityId;
import lombok.Getter;
import lombok.Setter;

public abstract class BaseStorageBusiness {

    @Getter
    @Setter
    private EntityId id;

    private String businessId;
    private String businessType;
    private String businessParams;
    private String publicFlag;

    public BaseStorageBusiness() {
        initialize();
    }

    protected void initialize() {}

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
