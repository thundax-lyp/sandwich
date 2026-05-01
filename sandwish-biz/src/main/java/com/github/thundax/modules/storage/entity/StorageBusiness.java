package com.github.thundax.modules.storage.entity;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.storage.entity.enums.StorageVisibility;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StorageBusiness {
    private EntityId id;

    private String businessId;
    private String businessType;
    private String businessParams;
    private StorageVisibility visibility = StorageVisibility.PRIVATE;

    public static final String BEAN_NAME = "ResourceBusiness";

    public void setVisibility(String visibility) {
        this.visibility = StringUtils.isBlank(visibility) ? null : StorageVisibility.from(visibility);
    }

    public void setVisibility(StorageVisibility visibility) {
        this.visibility = visibility;
    }

    public boolean isPublic() {
        return StorageVisibility.PUBLIC == getVisibility();
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

        public static final String PROP_VISIBILITY = "visibility";

        private String businessId;
        private String businessType;
        private String businessParams;

        private StorageVisibility visibility;

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

        public StorageVisibility getVisibility() {
            return visibility;
        }

        public void setVisibility(String visibility) {
            this.visibility = StringUtils.isBlank(visibility) ? null : StorageVisibility.from(visibility);
        }

        public void setVisibility(StorageVisibility visibility) {
            this.visibility = visibility;
        }
    }
}
