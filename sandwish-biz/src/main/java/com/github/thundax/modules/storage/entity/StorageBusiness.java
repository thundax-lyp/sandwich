package com.github.thundax.modules.storage.entity;

import com.github.thundax.common.config.Global;
import com.github.thundax.modules.storage.entity.base.BaseStorageBusiness;
import java.io.Serializable;
import org.apache.commons.lang3.StringUtils;

public class StorageBusiness extends BaseStorageBusiness {

    public static final String BEAN_NAME = "ResourceBusiness";

    public StorageBusiness() {
        super();
    }

    @Override
    protected void initialize() {
        this.setPublicFlag(Global.NO);
    }

    @Override
    public void setPublicFlag(String publicFlag) {
        super.setPublicFlag(StringUtils.equals(Global.YES, publicFlag) ? Global.YES : Global.NO);
    }

    public boolean isPublic() {
        return StringUtils.equals(Global.YES, getPublicFlag());
    }

    private Query query;

    public Query getQuery() {
        return this.query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public static class Query implements Serializable {

        public static final String PROP_BUSINESS_ID = "businessId";
        public static final String PROP_BUSINESS_TYPE = "businessType";
        public static final String PROP_BUSINESS_PARAMS = "businessParams";

        public static final String PROP_PUBLIC_FLAG = "publicFlag";

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
