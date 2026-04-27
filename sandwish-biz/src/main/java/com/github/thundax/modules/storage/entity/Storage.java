package com.github.thundax.modules.storage.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.thundax.common.config.Global;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.modules.storage.entity.base.BaseStorage;
import com.github.thundax.modules.storage.utils.MetaFile;
import java.io.Serializable;
import java.text.SimpleDateFormat;

/** @author */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Storage extends BaseStorage {

    private static final long serialVersionUID = 1L;

    public static final String BUSINESS_TYPE_UNDEFINED = "undefined";

    private static final String PATH_FORMAT = "yyyyMM";

    public static final String OWNER_TYPE_USER = "user";
    public static final String OWNER_TYPE_MEMBER = "member";

    public Storage() {
        super();
    }

    public Storage(String id) {
        super(id);
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.setEnableFlag(Global.ENABLE);
    }

    @Override
    public void setEnableFlag(String enableFlag) {
        super.setEnableFlag(
                StringUtils.equals(Global.ENABLE, enableFlag) ? Global.ENABLE : Global.DISABLE);
    }

    @JsonIgnore
    public boolean isEnable() {
        return StringUtils.equals(Global.ENABLE, getEnableFlag());
    }

    @JsonIgnore
    public String getFileName() {
        return this.getId() + MetaFile.DOT + this.getExtendName();
    }

    @JsonIgnore
    public String getOriginalFileName() {
        return this.getName() + MetaFile.DOT + this.getExtendName();
    }

    @JsonIgnore
    public String getPathName() {
        return new SimpleDateFormat(PATH_FORMAT).format(this.getCreateDate())
                + MetaFile.SEPARATOR
                + this.getFileName();
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

        private static final long serialVersionUID = 1L;

        public static final String PROP_MIME_TYPE = "mimeType";
        public static final String PROP_BUSINESS_ID = "businessId";
        public static final String PROP_BUSINESS_TYPE = "businessType";
        public static final String PROP_OWNER_ID = "ownerId";
        public static final String PROP_OWNER_TYPE = "ownerType";
        public static final String PROP_ENABLE_FLAG = "enableFlag";
        public static final String PROP_PUBLIC_FLAG = "publicFlag";
        public static final String PROP_NAME = "name";
        public static final String PROP_REMARKS = "remarks";

        private String mimeType;
        private String businessId;
        private String businessType;
        private String ownerId;
        private String ownerType;
        private String enableFlag;
        private String publicFlag;
        private String name;
        private String remarks;

        public String getMimeType() {
            return mimeType;
        }

        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
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

        public String getOwnerId() {
            return ownerId;
        }

        public void setOwnerId(String ownerId) {
            this.ownerId = ownerId;
        }

        public String getOwnerType() {
            return ownerType;
        }

        public void setOwnerType(String ownerType) {
            this.ownerType = ownerType;
        }

        public String getEnableFlag() {
            return enableFlag;
        }

        public void setEnableFlag(String enableFlag) {
            this.enableFlag = enableFlag;
        }

        public String getPublicFlag() {
            return publicFlag;
        }

        public void setPublicFlag(String publicFlag) {
            this.publicFlag = publicFlag;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRemarks() {
            return remarks;
        }

        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }
    }
}
